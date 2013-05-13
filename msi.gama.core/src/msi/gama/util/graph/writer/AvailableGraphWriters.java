package msi.gama.util.graph.writer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import msi.gama.runtime.exceptions.GamaRuntimeException;

/**
 * Lists available graphs writers, independently of the underlying library.
 * 
 * @author Samuel Thiriot
 *
 */
public class AvailableGraphWriters {


	private static final Map<String,Class<? extends IGraphWriter>> name2writer = new HashMap<String, Class<? extends IGraphWriter>>() {{
		
		// defaults 
		put("dgs",					GraphStreamWriterDGS.class);
		put("gml",					GraphStreamWriterGML.class);
		put("tikz",					GraphStreamWriterTikz.class);
		put("gexf",					GephiWriterGEXF.class);
		put("pajek",				GephiWriterPajek.class);
		//put("dl_list",				GephiWriterDLList.class);
		//put("ucinet_list",			GephiWriterDLList.class);
		//put("dl_matrix",			GephiWriterDLMatrix.class);
		//put("ucinet_matrix",		GephiWriterDLMatrix.class);
		put("csv",					GephiWriterCSV.class);

		put("graphml",				GraphStreamWriterGML.class);
		//put("tlp",					GephiWriterTLP.class);
		//put("tulip",				GephiWriterTLP.class);
		put("gdf",					GephiWriterGDF.class);
		put("guess",				GephiWriterGDF.class);
		
		// not ok: put("vna",					GephiWriterVNA.class);
		
		// graphstream
		put("graphstream.dgs",		GraphStreamWriterDGS.class);
		put("graphstream.gml",		GraphStreamWriterGML.class);
		put("graphstream.graphml",	GraphStreamWriterGML.class);
		put("graphstream.tikz",		GraphStreamWriterTikz.class);
		
		// prefuse
		// Nota: prefuse writers are not active now; they will probably no more be active later, because prefuse is not more maintained.
		// put("prefuse.gml",			PrefuseWriterGraphML.class);
		// put("prefuse.graphml",		PrefuseWriterGraphML.class);
		
		// gephi
		put("gephi.gexf",			GephiWriterGEXF.class);
		put("gephi.pajek",			GephiWriterPajek.class);
		//put("gephi.dl_list",		GephiWriterDLList.class);
		//put("gephi.ucinet_list",	GephiWriterDLList.class);
		//put("gephi.dl_matrix",		GephiWriterDLMatrix.class);
		//put("gephi.ucinet_matrix",	GephiWriterDLMatrix.class);
		put("gephi.graphml",		GephiWriterGraphML.class);
		put("gephi.tlp",			GephiWriterGraphML.class);
		put("gephi.tulip",			GephiWriterGraphML.class);
		put("gephi.gdf",			GephiWriterGDF.class);
		put("gephi.guess",			GephiWriterGDF.class);
		put("gephi.csv",			GephiWriterCSV.class);

		// not ok: put("gephi.vna",			GephiWriterVNA.class);
		
		
	}};
	
	public static Set<String> getAvailableWriters() {
		return name2writer.keySet();
	}
	
	private static Map<String, IGraphWriter> name2singleton = new HashMap<String, IGraphWriter>();
	
	public static IGraphWriter getGraphWriter(String name) {
		IGraphWriter res = name2singleton.get(name);
		
		if (res == null) {
			// no singleton created
			Class<? extends IGraphWriter> classWriter = name2writer.get(name);
			if (classWriter == null)
				throw GamaRuntimeException.error("unknown writer name: "+name+"; please choose one of "+getAvailableWriters().toString());
			try {
				res = classWriter.newInstance();
			} catch (InstantiationException e) {
				throw GamaRuntimeException.create(e);
			} catch (IllegalAccessException e) {
				throw GamaRuntimeException.create(e);
			}
			name2singleton.put(name, res);
		}
		
		return res;
	}
}
