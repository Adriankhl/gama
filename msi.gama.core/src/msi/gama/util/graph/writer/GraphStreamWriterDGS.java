/*******************************************************************************************************
 *
 * msi.gama.util.graph.writer.GraphStreamWriterDGS.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.util.graph.writer;

import msi.gama.ext.graphstream.FileSink;
import msi.gama.ext.graphstream.FileSinkDGS;

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
