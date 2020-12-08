/*

  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.uzk.hki.da.cb;

import static de.uzk.hki.da.utils.C.EVENT_TYPE_CONVERT;
import static de.uzk.hki.da.utils.C.METADATA_STREAM_ID_EPICUR;
import static de.uzk.hki.da.utils.C.OAI_DANRW_DE;
import static de.uzk.hki.da.utils.C.OWL_SAMEAS;
import static de.uzk.hki.da.utils.C.PUBLISHEDFLAG_INSTITUTION;
import static de.uzk.hki.da.utils.C.PUBLISHEDFLAG_PUBLIC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.Tika;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.PreconditionsNotMetException;
import de.uzk.hki.da.metadata.XepicurWriter;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.repository.MetadataIndex;
import de.uzk.hki.da.repository.MetadataIndexException;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.repository.RepositoryFacade;
import de.uzk.hki.da.util.ConfigurationException;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.StringUtilities;

/** 
 * This action implements the ingest into the presentation repository.
 * 
 * It creates the epicur metadata for URN/URL mapping and creates
 * one object and the in the collections open and closed collections
 * depending on the audiences it should be accessible to according
 * to the contract and sets the published flag in the database
 * accordingly.
 * 
 * The action also makes sure the metadata necessary for 
 * publication with OAI-PMH is present in the repository.
 * 
 * @author Sebastian Cuy
 * @author Daniel M. de Oliveira
 */
public class SendToPresenterAction extends AbstractAction {

	private static final String OPEN_COLLECTION_URI = "info:fedora/collection:open";
	private static final String CLOSED_COLLECTION_URI = "info:fedora/collection:closed";
	
	private static final String ddb = "ddb";
	private static final String OPENARCHIVES_OAI_IDENTIFIER = "http://www.openarchives.org/OAI/2.0/identifier";
	private static final String MEMBER = "info:fedora/fedora-system:def/relations-external#isMemberOf";
	private static final String MEMBER_COLLECTION = "info:fedora/fedora-system:def/relations-external#isMemberOfCollection";
	
	private RepositoryFacade repositoryFacade;
	private Map<String,String> viewerUrls;
	private Set<String> fileFilter;
	private Map<String,String> labelMap;
	
	private MetadataIndex metadataIndex;
	private String indexName;
	
	
	@Override
	public void checkConfiguration() {
		if (repositoryFacade == null) 
			throw new ConfigurationException("repositoryFacadeRepository");
		if (viewerUrls == null)
			throw new ConfigurationException("viewerUrls");
		if (fileFilter == null)
			throw new ConfigurationException("fileFilter");
		if (testContractors == null)
			throw new ConfigurationException("testContractors");
	}
	

	@Override
	public void checkPreconditions() {
		if (o.getUrn()==null||o.getUrn().isEmpty())
			throw new PreconditionsNotMetException("urn not set");
	}
	
	/**
	 * Preconditions:
	 * There can be two pips at
	 * workAreaRoot/pips/public/shortname/oid
	 * workAreaRoot/pips/insitution/shortname/oid
	 * Each of them must contain a DC.xml with a format tag set to one of the supported formats.
	 * For every format tag which can exist there must be a viewer url configured
	 * @throws RepositoryException 
	 * 
	 */
	@Override
	public boolean implementation() throws IOException, RepositoryException {
		
		if (StringUtilities.isNotSet(preservationSystem.getOpenCollectionName()))
			throw new IllegalStateException("open collection name must be set");
		if (StringUtilities.isNotSet(preservationSystem.getClosedCollectionName()))
			throw new IllegalStateException("closed collection name must be set");
		
		
		purgeObjectsIfExist();
		buildMapWithOriginalFilenamesForLabeling();
		try {
			getMetadataIndex().deleteFromIndex(adjustIndexName(indexName), o.getIdentifier());
		} catch (MetadataIndexException e1) {
			new MetadataIndexException("Unable to delete data from index!"+e1);
		}
		
		boolean publicPIPSuccessfullyIngested = false;
		boolean institutionPIPSuccessfullyIngested = false;
		try {
			
			if (wa.pipFolder(WorkArea.PUBLIC).toFile().exists()) {
				publishPackage(
					WorkArea.PUBLIC,true,preservationSystem.getOpenCollectionName());
				publicPIPSuccessfullyIngested=true;
				logger.debug("publ pip ingested");
			}
			
			if (wa.pipFolder(WorkArea.WA_INSTITUTION).toFile().exists()) { 
				publishPackage(
					WorkArea.WA_INSTITUTION,false,preservationSystem.getClosedCollectionName());
				institutionPIPSuccessfullyIngested = true;
				logger.debug("inst pip ingested");
			}
			
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
		
		setPublishedFlag(publicPIPSuccessfullyIngested,
				institutionPIPSuccessfullyIngested);
		if (StringUtilities.isNotSet(o.getPackage_type())||(!publicPIPSuccessfullyIngested)) {
			setKILLATEXIT(true); // indexing and creating edm not possible
			o.setObject_state(Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
		}
		return true;
	}
	
	
	@Override
	public void rollback() {
		purgeObjectsIfExist();
		setPublishedFlag(false, false);
		deleteXepicur();
	}

	private void deleteXepicur() {
		
		wa.pipMetadataFile(WorkArea.WA_INSTITUTION,METADATA_STREAM_ID_EPICUR).delete();
		wa.pipMetadataFile(WorkArea.PUBLIC,METADATA_STREAM_ID_EPICUR).delete();
	}
	
	
	
	
	

	/**
	 * 
	 * @param pipType institution or public
	 * @return
	 * @throws IOException 
	 * @throws RepositoryException 
	 */
	private void publishPackage(String pipType,boolean checkSets, String collectionName) throws IOException, RepositoryException {
		
		String pkgType = o.getPackage_type(); 
		if (!viewerUrls.containsKey(pkgType))
			logger.warn("could not determine a viewerUrl for package type of pip institution");
		
		XepicurWriter.createXepicur(
				o.getIdentifier(), pkgType, 
				viewerUrls.get(pkgType), 
				wa.pipMetadataFile(pipType,"epicur"),preservationSystem.getUrnNameSpace(),preservationSystem.getUrisFile());
		
		ingestPackage(o.getUrn(), o.getIdentifier(), collectionName, wa.pipFolder(pipType), 
				o.getContractor().getShort_name(), pkgType, makeSets(checkSets));
		addRelsExtRelationships(collectionName,makeSets(checkSets));
	}
	
	
	
	private void addRelsExtRelationships(String collection,String[] sets) throws RepositoryException {
		
		// add RELS-EXT relationships
		try {
	
			// add urn as owl:sameAs
			repositoryFacade.addRelationship(o.getIdentifier(), collection, OWL_SAMEAS, o.getUrn());
			logger.debug("Added relationship: "+OWL_SAMEAS+" "+o.getUrn());
			
			// add collection membership
			String collectionUri;
			if (preservationSystem.getClosedCollectionName().equals(collection)) {
				collectionUri = CLOSED_COLLECTION_URI;
			} else {
				collectionUri = OPEN_COLLECTION_URI;
			}
			repositoryFacade.addRelationship(o.getIdentifier(), collection, MEMBER_COLLECTION, collectionUri);
			logger.debug("Added relationship: "+MEMBER_COLLECTION+" "+ collectionUri);
			
			// add oai identifier
			if (!(preservationSystem.getClosedCollectionName()+":").equals(collection))
			) {
				String oaiId = OAI_DANRW_DE + o.getIdentifier();
				repositoryFacade.addRelationship(o.getIdentifier(), collection, OPENARCHIVES_OAI_IDENTIFIER, oaiId);
				logger.debug("Added relationship: "+OPENARCHIVES_OAI_IDENTIFIER+" " + oaiId);
			}
			
			// add oai sets
			if (sets != null) for (String set : sets) {
				repositoryFacade.addRelationship(o.getIdentifier(), collection, MEMBER, "info:fedora/set:" + set);
				logger.debug("Added relationship: "+MEMBER+" info:fedora/set:" + set);
			}
			
		} catch (Exception e) {
			throw new RepositoryException("Failed to add relationships for package in fedora",e);
		}
	}


	private String[] makeSets(boolean checkSets) {
		String[] sets = null;
		if (checkSets){
			if (!o.ddbExcluded()) {
				sets = new String[]{ ddb };
			}
		}
		return sets;
	}
	


	/**
	 * build map that contains original filenames for labeling
	 */
	private void buildMapWithOriginalFilenamesForLabeling() {
		labelMap = new HashMap<String,String>();
		for (Event e:o.getLatestPackage().getEvents()) {			
			if (!EVENT_TYPE_CONVERT.equals(e.getType())) continue;
			DAFile targetFile = e.getTarget_file();
			if (!targetFile.getRep_name().startsWith(WorkArea.TMP_PIPS)) continue;			
			DAFile sourceFile = e.getSource_file();
			labelMap.put(targetFile.getRelative_path(), sourceFile.getRelative_path());			
		}
	}
	
	
	/**
	 * delete existing packages before ingesting the new ones
	 * @throws RuntimeException
	 */
	private void purgeObjectsIfExist(){
		try {
			logger.debug("purging: "+preservationSystem.getOpenCollectionName()+":"+o.getIdentifier());
			logger.debug("purging: "+preservationSystem.getClosedCollectionName()+":"+o.getIdentifier());
			repositoryFacade.purgeObjectIfExists(o.getIdentifier(), preservationSystem.getOpenCollectionName());
			repositoryFacade.purgeObjectIfExists(o.getIdentifier(), preservationSystem.getClosedCollectionName());
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}
	
	

	/**
	 * @param publicPIPSuccesfullyIngested
	 * @param institutionPIPSuccessfullyIngested
	 */
	private void setPublishedFlag(
			boolean publicPIPSuccesfullyIngested,
			boolean institutionPIPSuccessfullyIngested) {
		
		int publishedFlag = 0;

		if (publicPIPSuccesfullyIngested) publishedFlag += PUBLISHEDFLAG_PUBLIC;
		if (institutionPIPSuccessfullyIngested) publishedFlag += PUBLISHEDFLAG_INSTITUTION;

		o.setPublished_flag(publishedFlag);
		logger.debug("Set published flag of object to '{}'", o.getPublished_flag());
	}

	
	/**
	 * @param urn
	 * @param objectId
	 * @param collection
	 * @param packagePath
	 * @param contractorShortName
	 * @param packageType
	 * @param sets
	 * @return
	 * @throws RepositoryException
	 * @throws IOException
	 */
	private void ingestPackage(String urn, String objectId, String collection,
			Path packagePath, String contractorShortName, String packageType,
			String[] sets) throws RepositoryException, IOException {
		
		File pip = Path.makeFile(packagePath);
		if (!pip.exists()) {
			throw new FileNotFoundException("Missing file or directory: " + pip);
		}

		if (!repositoryFacade.objectExists(objectId, collection)) {
			repositoryFacade.createObject(objectId, collection, contractorShortName);
		}			
		ingestDirectoryContentAsDatastreamsIntoRepository(objectId, collection, pip, packagePath, packageType);
	}


	

	private void ingestDirectoryContentAsDatastreamsIntoRepository(String objectId, String collection, File dir, Path packagePath, String packageType) throws RepositoryException, IOException {
			
		File files[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (getFileFilter().contains(name)) return false;
				else return true;
			}
		});

		if(files != null) {
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					ingestDirectoryContentAsDatastreamsIntoRepository(objectId, collection, files[i], packagePath, packageType);
				} else {
					ingestFile(objectId, collection, files[i], packagePath, packageType);
				}
			}
		}
	}	

	
	
	/**
	 * Either ingests a file into the repositoryFacade or, in case it is a metadataFile,
	 * it creates a the correspoding metadata datasream via repositoryFacade
	 * 
	 * @param objectId
	 * @param collection
	 * @param file
	 * @param packagePath
	 * @param packageType
	 * @return
	 * @throws RepositoryException
	 * @throws IOException
	 */
	private boolean ingestFile(String objectId, String collection, File file, Path packagePath, String packageType) throws RepositoryException, IOException {

		// Detect MIME-Type
		String mimeType = detectMimeType(file);
		
		// Generate datastream id
		String relPath = file.getAbsolutePath().replace(packagePath + "/","");
		String fileId = repositoryFacade.generateFileId(relPath);
		
		String label = file.getName();
		if (labelMap.containsKey(fileId)) {
			label = labelMap.get(fileId);
		}
		
		repositoryFacade.ingestFile(objectId, collection, fileId, file, label, mimeType);
		logger.info("Successfully created datastream with fileId {} for file {}.",fileId,file.getName());
		return true;
	
	}
	
	private String detectMimeType(File file) throws IOException {
		
		// TODO: read from premis when available or: better: provide in DAFile!
		
		String mimeType;
	    Tika tika = new Tika();
	    try {
	        mimeType = tika.detect(file);
	    }  catch (IOException e) {
	        throw new IOException("Unable to open file for mime type detection: " + file.getAbsolutePath(), e);
	    }
		logger.debug("Detected MIME type {} for file {}",mimeType,file.getName());		
		return mimeType;
		
	}	
	
	/**
	 * use test index for test packages
	 * @param originalIndexName
	 * @return
	 */
	private String adjustIndexName(String originalIndexName){
		
		String contractorShortName = o.getContractor().getShort_name();
		String adjustedIndexName = indexName;
		if(testContractors != null && testContractors.contains(contractorShortName)) {
			adjustedIndexName += MetadataIndex.TEST_INDEX_SUFFIX;;
		}
		return adjustedIndexName;
	}

	/**
	 * Get the set of contractors that are considered test users.
	 * Objects ingested by these users will not be published 
	 * in the OAI-PMH server.
	 * @return the set of test users
	 */
	public Set<String> getTestContractors() {
		return testContractors;
	}

	/**
	 * Set the set of contractors that are considered test users.
	 * Objects ingested by these users will not be published 
	 * in the OAI-PMH server.
	 * @param the set of test users
	 */
	public void setTestContractors(Set<String> testContractors) {
		this.testContractors = testContractors;
	}

	/**
	 * For every package type read from the DC.xml (like "EAD"), there is a viewer url 
	 * @return
	 */
	public Map<String,String> getViewerUrls() {
		return viewerUrls;
	}

	public void setViewerUrls(Map<String,String> viewerUrls) {
		this.viewerUrls = viewerUrls;
	}
	
	/**
	 * Get the repository facade
	 * @return the repository facade
	 */
	public RepositoryFacade getRepositoryFacade() {
		return repositoryFacade;
	}
	
	/**
	 * Set the repository facade
	 * @param the repository facade
	 */
	public void setRepositoryFacade(RepositoryFacade repositoryFacade) {
		this.repositoryFacade = repositoryFacade;
	}
	
	/**
	 * Get the set of filenames to be filtered during ingest
	 * @return the set of filenames to be filtered during ingest
	 */
	public Set<String> getFileFilter() {
		return fileFilter;
	}
	
	/**
	 * Set the set of filenames to be filtered during ingest
	 * @param fileFilter the set of filenames to be filtered during ingest
	
	 */
	public void setFileFilter(Set<String> fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	public MetadataIndex getMetadataIndex() {
		return metadataIndex;
	}
	
	public void setMetadataIndex(MetadataIndex mi) {
		this.metadataIndex = mi;
	}
	
	/**
	 * Get the name of the index
	 * the data will be indexed in.
//	 * @return the index name
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * Set the name of the index
	 * the data will be indexed in.
	 * @param the index name
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
}