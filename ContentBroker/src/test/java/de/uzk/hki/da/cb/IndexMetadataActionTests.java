/*
  DA-NRW Software Suite | ContentBroker
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

import org.junit.Test;

/**
 * @author Daniel M. de Oliveira
 */
public class IndexMetadataActionTests {
	
	@Test
	public void test() {
		
	}

//	private static int invocations;
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void test() throws RepositoryException, IOException{
//		Object object = TESTHelper.setUpObject("123", new RelativePath("workArea"));
//		
//		IndexMetadataAction action = new IndexMetadataAction();
//		
//		PreservationSystem ps = new PreservationSystem();
//		ps.setOpenCollectionName("collection-open");
//		action.setPSystem(ps);
//		
//		action.setObject(object);
//		
//		Set<String> testContractors = new HashSet<String>();
//		action.setTestContractors(testContractors);
//		
//		Map<String,String> frames = new HashMap<String,String>();
//		frames.put("src/main/resources/frame.jsonld","EDM");
//		action.setFrames(frames);
//		
//		action.setIndexName("indexName");
//		RepositoryFacade repositoryFacade = mock(Fedora3RepositoryFacade.class);
//		InputStream inputStream = new FileInputStream("src/test/resources/cb/IndexMetadataActionTests/edm.xml");
//		
//		invocations = 0;
//		try {
//			when(repositoryFacade.retrieveFile(anyString(),anyString(),anyString())).thenReturn(inputStream);
//			doAnswer(new Answer<Void>(){
//				@Override
//		        public Void answer(InvocationOnMock invocation) throws Throwable {
//					if ("1-20140702256699".equals((invocation.getArguments()[2]))){
//						invocations++;
//						if (!
//							((Map<String,java.lang.Object>)invocation.getArguments()[3]).toString()
//								.equals("{@id=http://da-nrw-q.lvr.de/aggregation/1-20140702256699, @type=ore:Aggregation, edm:aggregatedCHO={@id=http://da-nrw-q.lvr.de/cho/1-20140702256699, @type=edm:ProvidedCHO, dc:identifier=[VDA, http://da-nrw-q.lvr.de/cho/1-20140702256699], dc:title=[VDA - Forschungsstelle Rheinlländer in aller Welt: Bezirksstelle West des Vereins für das Deutschtum im Ausland], dc:type=[collection], dcterms:date=1920-1942, edm:type=}, edm:dataProvider=, edm:provider=, edm:rights={@id=http://www.europeana.eu/rights/rr-r/}, @context=nullframe.jsonld}")
//								)
//								fail();
//					}
//		            return null;
//		        }
//			}).when(repositoryFacade).
//			indexMetadata(anyString(), anyString(), anyString(), 
//					(Map<String,java.lang.Object>)anyObject()
//					);
//		} catch (RepositoryException e) {fail();}
//		
//		
//		action.setRepositoryFacade(repositoryFacade);
//		
//		action.implementation();
//		assertTrue(invocations==1);
//	}
}
