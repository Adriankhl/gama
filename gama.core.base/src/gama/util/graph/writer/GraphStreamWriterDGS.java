/*******************************************************************************************************
 *
 * gama.util.graph.writer.GraphStreamWriterDGS.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.util.graph.writer;

import gama.core.ext.graphstream.FileSink;
import gama.core.ext.graphstream.FileSinkDGS;

/**
 * @see http://graphstream-project.org/doc/Advanced-Concepts/The-DGS-File-Format/
 * @author Samuel Thiriot
 *
 */
public class GraphStreamWriterDGS extends GraphStreamWriterAbstract {

	public static final String NAME = "dgs";
	
	@Override
	public FileSink getFileSink() {
		return new FileSinkDGS();
	}

}
