	/*
	  DA-NRW Software Suite | ContentBroker
	  Copyright (C) 2014 Historisch-Kulturwissenschaftliche Informationsverarbeitung
	  Universität zu Köln
	  Copyright (C) 2015 LVRInfoKom
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
	
# Erweiterte Spezifikation PIP (Presentation Information Package) zur Anbindung von DiPS.komunal an die Präsentationsschicht.

Die Spezifikation erweitert die [aktuell geltende PIP-Spezifikation für DNS](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_pip.de.md). Die Spezifikation wird erweitert, um z.B.
* Besonderheiten der aus DiPS.kommunal gelieferten Metadaten in der Präsentationsschicht verarbeiten zu können (Bsp. zu registrierender URN, damit Inhalte aius dem Portal Archiv.NRW adressiert werden können)
* Weitere Funktionalitäten bereitzustellen (z.B. Erkennung, ob ein URN in der Präsentationsschicht erzeugt werden muss oder nicht)

## Zusammensetzung eines PIP 

Ein **PIP** besteht aus einer oder mehreren Inhalts-Dateien, die sich über Web-Portale oder an Web-Viewer darstellen lassen und weiteren Dateien, die Metadaten über das PIP enthalten. Alle Metadaten und deren referenzierten Dateien müssen konsistent sein. Entsprechend des noch zu vereinbarenden Transportweges werden diese Dateien entweder als einzelne Dateien oder in einem zu spezifizierenden Containerformat zum Presentation Repository des DNS übertragen. 

## Struktur des PIP

Auf der Strukturebene setzt sich ein PIP immer aus den Bestandteilen Inhaltsdatenströme und Metadatenströme zusammen. Die Anzahl der Datenströme kann sich unterscheiden,   

    Inhaltsdatei_1.yxz (verpflichtend)
    ...
    Inhaltsdatei_n.yxz (optional)
    
    EDM.xml (verpflichtend)
    DC.xml (verpflichtend)
   
    Optional eins der weiteren empfohlene Metadatenformate
    LIDO.xml
    METS/Mods.xml
    EAD.xml
    

### Inhaltsdatei(en)
Eines PIP muss mindestens eine Inhaltsdatei, kann jedoch bis zu **n** Inhaltsdateien enthalten. PIP ohne Inhaltsdatei können von der Präsentationsschicht des DA NRW nicht den Vorgaben entsprechend verarbeitet werden und müssen bereits bei der Einieferung abgelehnt werden.   

### Metadatendateien

**EDM**

Jedes PIP muss genau eine Datei mit Metadaten entsprechend des Europeana Data Model (EDM) enthalten. Diese muss im XML-Format vorliegen und den Vorgaben der Europeana hinsichtlich der Ablieferung an die Europeana genügen. Die [Dokumentation](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_edm_fields_to_provide.md) gibt einen ersten Überblick und liefert Vorgaben für spezielle Felder.  Die genauen Informationen für die Implementierung finden sich in den [EDM-Guidelines 2017](https://pro.europeana.eu/files/Europeana_Professional/Share_your_data/Technical_requirements/EDM_Documentation/EDM_Mapping_Guidelines_v2.4_102017.pdf).

Die Präsentationsschicht verwendet die im EDM-Format gelieferten Metadaten für die Suche und Darstellung der PIP im DA NRW-Portal **und** für die optionale Aggregation für DDB und Europeana. Die EDM-Datei muss deshalb enthalten sein.

**DC Simple**

Jedes PIP muss eine (DC.xml) Datei mit Metadaten entsprechend des Dublin Core-Standards enthalten. Diese wird an der OAI-Schnittstelle des Presentation Repositorys als Standard-Ausgabeformat (oai_dc) präsentiert. Damit das Mapping der Metadaten aus dem Ursprungsformat möglichst den Anforderungen des Servicenehmers entspricht, ist die DC.xml ebenfalls als bestandteil des PIP einzuliefern.    

**Weitere Metadaten-Formate**

Als weiteres Metadatenformat sollte wenn möglich auch eine spartenspezifische Beschreibung in LIDO, EADS oder METS/Mods als Bestandteil des PIP vorliegen. 

Die in der bisherigen Spezifikation als Pflichtbestandteil genannte epicur.xml-Datei wird künftig grundsätzlich durch das Presentation Repository generiert. Sie dient der Registrierung eines URN per OAI-Schnittstelle. Die Erzeugung im Presentation Repository führt zu einem robusteren Registrierungsworkflow. 

## Vorgaben für die Metadatenformate

Detailierte Vorgaben für die verwendeten Metadatenformate sind entsprechend der folgenden Tabelle abrufbar


### EDM

* [Europeana Mapping Guidelines 2017](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_edm_fields_to_provide.md)
* [Übersicht](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_edm_fields_to_provide.md) zur Nutzung von EDM in der Präsentationsschicht des DA NRW

PIP-spezifische Vorgaben:


### LIDO.xml

* Formatbeschreibung siehe [Schema v1.0](http://lido-schema.org/schema/v1.0/lido-v1.0-schema-listing.html)
* Mappingbeschreibung [LIDO -> EDM](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_lido2edm.md)

### EAD.xml

* Formatbeschreibung siehe [offizielle EAD Spezifikation](http://www.loc.gov/ead/index.html)
* Spezielle [DDB-EAD-Spezifikation](http://www.dlib.indiana.edu/services/metadata/activities/EADManual.pdf)
* Mappingbeschreibung [EAD -> EDM](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_ead2edm.md)

### Mods METS.xml
 
* Formatbeschreibung siehe [offizielle METS Spezifikation](http://www.loc.gov/standards/mets/)
* Mappingbeschreibung [METS -> EDM](https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/markdown/specification_mets2edm.de.md)

