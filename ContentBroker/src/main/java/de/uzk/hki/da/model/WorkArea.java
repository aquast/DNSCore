/*
 DA-NRW Software Suite | ContentBroker
 Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
 Universität zu Köln
 Copyright (C) 2015 LVR-Infokom
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

package de.uzk.hki.da.model;

import static de.uzk.hki.da.core.C.*;
import static de.uzk.hki.da.utils.StringUtilities.*;

import java.io.File;

import de.uzk.hki.da.util.Path;

/**
 * Knows how the WorkArea is structured and how files and objects are organized on it.
 * Knows about the dnscore data model.
 * 
 * @author Daniel M. de Oliveira
 *
 */
public class WorkArea {

	private static final String ERR_MSG_NULL_OR_EMPTY = "Must not be null or empty: ";
	private static final String ERR_MSG_MISSING = "Missing file or directory: ";
	
	private Object o;
	private Node n;
	
	public WorkArea(Node n,Object o){
		this.o = o;
		this.n = n;
		
		if (o.getContractor()==null) throw new IllegalArgumentException(ERR_MSG_NULL_OR_EMPTY+"o.getContractor()");
		if (o.getIdentifier()==null) throw new IllegalArgumentException(ERR_MSG_NULL_OR_EMPTY+"o.getIdentifier()");
		if (isNotSet(n.getWorkAreaRootPath())) throw new IllegalArgumentException(ERR_MSG_NULL_OR_EMPTY+"n.getWorkAreaRootPath()");
		if (isNotSet(o.getContractor().getShort_name())) throw new IllegalArgumentException(ERR_MSG_NULL_OR_EMPTY+"o.getContractor().getShort_name()");

		if (!n.getWorkAreaRootPath().toFile().exists()) throw new IllegalArgumentException(ERR_MSG_MISSING+n.getWorkAreaRootPath());
		if (!contractorPipSDir(WA_PUBLIC).exists()) throw new IllegalArgumentException(ERR_MSG_MISSING+contractorPipSDir(WA_PUBLIC));
		if (!contractorPipSDir(WA_INSTITUTION).exists()) throw new IllegalArgumentException(ERR_MSG_MISSING+contractorPipSDir(WA_INSTITUTION));
		if (!contractorWorkDir().exists()) throw new IllegalArgumentException(ERR_MSG_MISSING+contractorWorkDir());
	}

	public File toFile(DAFile daf) {
		return Path.make(contractorWorkDirPath(),o.getIdentifier(),daf.getRep_name(),daf.getRelative_path()).toFile();
	}

	public Path pipFolder(String audience) {
		return Path.make(contractorPipsDirPath(audience),o.getIdentifier());
	}

	public File metadataStream(String audience,String metadataFileName) {
		return Path.makeFile(contractorPipsDirPath(audience),o.getIdentifier(),metadataFileName+FILE_EXTENSION_XML);
	}

	private File contractorPipSDir(String audience) {
		return contractorPipsDirPath(audience).toFile();
	}
	
	private Path contractorPipsDirPath(String audience) {
		return Path.make(n.getWorkAreaRootPath(),WA_PIPS,audience,o.getContractor().getShort_name());
	}
	
	private File contractorWorkDir() {
		return contractorWorkDirPath().toFile();
	}

	private Path contractorWorkDirPath() {
		return Path.make(n.getWorkAreaRootPath(),WA_WORK,o.getContractor().getShort_name());
	}
	
	
	
}
