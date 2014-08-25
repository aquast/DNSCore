/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln
  Copyright (C) 2014 LVRInfoKom
  Landschaftsverband Rheinland

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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.uzk.hki.da.core.ConfigurationException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.core.UserException.UserExceptionId;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.service.MailContents;
import de.uzk.hki.da.utils.ArchiveBuilder;
import de.uzk.hki.da.utils.ArchiveBuilderFactory;
import de.uzk.hki.da.utils.BagitUtils;
import de.uzk.hki.da.utils.Path;


/**
 * Retrieves Packages from the DataGrid.
 * Does a MD5 Check and copies DIP to the outgoing folder of User. 
 * @author Jens Peters
 * @author Daniel M. de Oliveira
 * @author Thomas Kleinke
 */

public class RetrievalAction extends AbstractAction {
	
	private Path newTar;


	@Override
	void checkActionSpecificConfiguration() throws ConfigurationException {
		// Auto-generated method stub
	}

	
	
	
	@Override
	void checkSystemStatePreconditions() throws IllegalStateException {
		// Auto-generated method stub
	}

	
	
	
	@Override
	protected
	boolean implementation() throws IOException {

		newTar = Path.make(localNode.getUserAreaRootPath(),object.getContractor().getShort_name(),"outgoing",object.getIdentifier() + ".tar");
		
		Path tempFolder = createTmpFolder();
		

		if (job.getQuestion()==null||job.getQuestion().isEmpty()){
			stdRetrieval(tempFolder);
		}
		else {
			System.out.println("other");
			return true;
		}
		
		
		bagitAndTarit(tempFolder);

		cleanupFS();
		
		new MailContents(preservationSystem,localNode).retrievalReport(object);
		return true;
	}

	
	@Override
	void rollback() {
		
		newTar.toFile().delete();
	}
	
	private void stdRetrieval(Path tempFolder) throws IOException{
		moveNewestPremisToDIP(tempFolder);
		copySurfaceRepresentation(tempFolder);
	}




	private void moveNewestPremisToDIP(Path tempFolder) throws IOException {
		File premisFile = Path.makeFile(object.getDataPath(),object.getNameOfNewestBRep(),"premis.xml");
		if (!premisFile.exists()) throw new RuntimeException("CRITICAL ERROR: premis file could has not been found");
		File dest = Path.makeFile(tempFolder,"data","premis.xml");
		FileUtils.copyFile(premisFile, dest);
	}




	private Path createTmpFolder(){
		Path tmpFolder = Path.make(localNode.getWorkAreaRootPath(),"work",
				object.getContractor().getShort_name(), object.getIdentifier(), object.getIdentifier()); 
		tmpFolder.toFile().mkdir();
		return tmpFolder;
	}




	private void bagitAndTarit(Path tempFolder){

		BagitUtils.buildBagit(tempFolder.toString());
		
		logger.debug("Building tar at " + newTar);
		try {
			ArchiveBuilder builder = ArchiveBuilderFactory.getArchiveBuilderForFile(new File(".tar"));
			builder.archiveFolder(tempFolder.toFile(),
							  newTar.toFile(), true);
		} catch (Exception e) {
			throw new RuntimeException("Tar couldn't be packed", e);
		} 
	}
	
	
	private void cleanupFS() throws IOException{
		
		// cleanup
		String relativePackagePath = object.getContractor().getShort_name() + "/" + object.getIdentifier() + "/";
		File packageFolder = Path.makeFile(localNode.getWorkAreaRootPath(),"work",relativePackagePath);
		
		FileUtils.deleteDirectory(packageFolder);
	}
	
	
	
	/**
	 * @param destinationFolder
	 * @throws RuntimeException
	 */
	private void copySurfaceRepresentation(Path destinationFolder)
			throws RuntimeException {
		
		List<DAFile> files = object.getNewestFilesFromAllRepresentations(preservationSystem.getSidecarExtensions());
		for (DAFile f : files)
		{
			if (f.toRegularFile().getName().equals("premis.xml")) continue;
				
			File dest = Path.makeFile(destinationFolder,"data",f.getRelative_path());
			logger.info("file will be part of dip: "+dest.getAbsolutePath());
			String destFolder = dest.getAbsolutePath().substring(0, dest.getAbsolutePath().lastIndexOf("/"));

			new File(destFolder).mkdirs();

			try {
				FileUtils.copyFile(f.toRegularFile(), dest);
			} catch (IOException e) {
				throw new UserException(UserExceptionId.RETRIEVAL_ERROR, "Couldn't copy file " + f.toRegularFile().getAbsolutePath() + " to folder " + destFolder, e);
			}
		}
	}
}
