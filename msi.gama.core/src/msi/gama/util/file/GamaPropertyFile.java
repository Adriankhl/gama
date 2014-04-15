/*********************************************************************************************
 * 
 *
 * 'GamaPropertyFile.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.util.file;

import java.io.*;
import java.util.Properties;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.types.IType;
import com.vividsolutions.jts.geom.Envelope;

@file(name = "property", extensions = { "properties" }, buffer_type = IType.MAP, buffer_content = IType.STRING, buffer_index = IType.STRING)
public class GamaPropertyFile extends GamaFile<GamaMap<String, String>, GamaPair<String, String>, String, String> {

	public GamaPropertyFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		Properties p = new Properties();
		GamaMap m = new GamaMap();
		FileReader f = null;
		try {
			f = new FileReader(getFile());
			p.load(f);
		} catch (FileNotFoundException e) {
			throw GamaRuntimeException.create(e);
		} catch (IOException e) {
			throw GamaRuntimeException.create(e);
		} finally {
			if ( f != null ) {
				try {
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		m.putAll(p);
		setBuffer(m);
	}

	@Override
	protected void flushBuffer() throws GamaRuntimeException {
		// TODO A faire
	}

	@Override
	public Envelope computeEnvelope(final IScope scope) {
		// TODO Probably possible to get some information there
		return null;
	}

}
