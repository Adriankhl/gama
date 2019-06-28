/*******************************************************************************************************
 *
 * msi.gama.util.graph.loader.GraphstreamGraphParserGraphML.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.util.graph.loader;

import msi.gama.util.graph.graphstream_copy.FileSource;
import msi.gama.util.graph.graphstream_copy.FileSourceGraphML;

public class GraphstreamGraphParserGraphML extends GraphStreamGraphParserAbstract {

	@Override
	protected FileSource getFileSource() {
		return new FileSourceGraphML();
	}

}
