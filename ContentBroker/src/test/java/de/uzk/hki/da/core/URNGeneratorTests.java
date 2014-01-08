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

package de.uzk.hki.da.core;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.hibernate.classic.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.db.HibernateUtil;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.service.RegisterObjectService;
import de.uzk.hki.da.service.URNCheckDigitGenerator;
import de.uzk.hki.da.utils.Utilities;


/**
 * The Class URNGeneratorTests.
 */
public class URNGeneratorTests {

	
	/**
	 * TODO Up til now i couldn't find a sample test suite of valid URNs.
	 * Tests URNCheckDigitGenerator.checkDigit
	 * @author: Daniel M. de Oliveira
	 */
	@Test
	public void testCheckDigit(){
		
		String base = "urn:nbn:de:gbv:089-332175294";
		
		assertEquals(new URNCheckDigitGenerator().checkDigit( base ),"5");
	}
	
	/**
	 * Sets the up.
	 */
	@Before
	public void setUp(){
		HibernateUtil.init("src/main/conf/hibernateCentralDB.cfg.xml.inmem");
		Session session = HibernateUtil.openSession();
		session.getTransaction().begin();
		session.createSQLQuery("DELETE FROM nodes").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
	
	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {
		Session session = HibernateUtil.openSession();
		session.getTransaction().begin();
		session.createSQLQuery("DELETE FROM nodes").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
	
	
	/**
	 * This test excludes checking the checkdigit.
	 * For a test of the checkdigit please refer to {@link #testCheckDigit()} 
	 * 
	 * Tests URNGenerator.generateURNForNode
	 * @author: Daniel M. de Oliveira
	 */
	@Test
	public void testGenerateURNForNode(){
		
		Node node = new Node("vm1","vm1-01");
		node.setUrn_index(92);
		Session session = HibernateUtil.openSession();
		session.getTransaction().begin();
		session.save(node);
		session.getTransaction().commit();
		session.close();

		
		
		
		RegisterObjectService registerObjectService = new RegisterObjectService();
		registerObjectService.setNameSpace("urn:nbn:de:danrw");
		
		String urnWithCheckDigit = registerObjectService.generateURNForNode(node,new Date());
		String urnWithoutCheckDigit = urnWithCheckDigit.substring(0, urnWithCheckDigit.length()-1);
		
		assertEquals(("urn:nbn:de:danrw-"+node.getId()+"-"+ Utilities.todayAsSimpleIsoDate(new Date())+"93"),urnWithoutCheckDigit );
			
		
	}
	
}
