/*********************************************************************************************
 * 
 * 
 * 'GamaTextFile.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.util.file;

import java.io.*;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.types.*;
import com.vividsolutions.jts.geom.Envelope;

@file(name = "text",
	extensions = { "txt", "data", "csv", "text", "tsv", "xml" },
	buffer_type = IType.LIST,
	buffer_content = IType.STRING,
	buffer_index = IType.INT)
public class GamaTextFile extends GamaFile<IList<String>, String, Integer, String> {

	public GamaTextFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	@Override
	public IContainerType getType() {
		return Types.FILE.of(Types.INT, Types.STRING);
	}

	public GamaTextFile(final IScope scope, final String pathName, final IList<String> text) {
		super(scope, pathName, text);
	}

	@Override
	public String _stringValue(final IScope scope) throws GamaRuntimeException {
		getContents(scope);
		StringBuilder sb = new StringBuilder(getBuffer().length(scope) * 200);
		for ( String s : getBuffer().iterable(scope) ) {
			sb.append(s).append("\n"); // TODO Factorize the different calls to "new line" ...
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if ( getBuffer() != null ) { return; }
		try {
			final BufferedReader in = new BufferedReader(new FileReader(getFile()));
			final IList<String> allLines = GamaListFactory.create(Types.STRING);
			String str;
			str = in.readLine();
			while (str != null) {
				allLines.add(str);
				str = in.readLine();
			}
			in.close();
			setBuffer(allLines);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.util.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer() throws GamaRuntimeException {
		// TODO A faire.

	}

	@Override
	public Envelope computeEnvelope(final IScope scope) {
		return null;
	}

}
