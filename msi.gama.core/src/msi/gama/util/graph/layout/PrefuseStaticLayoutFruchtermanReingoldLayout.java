package msi.gama.util.graph.layout;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import msi.gama.runtime.exceptions.GamaRuntimeException;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;

/**
 * @see http://prefuse.org/doc/api/prefuse/action/layout/graph/FruchtermanReingoldLayout.html
 * @author Samuel Thiriot
 *
 */
public class PrefuseStaticLayoutFruchtermanReingoldLayout extends
		PrefuseStaticLayoutAbstract {

	public static final String NAME = "fruchtermanreingold";

	public static final String OPTION_NAME_MAXITER = "maxiter";
	
	@Override
	protected Layout createLayout(long timeout, Map<String, Object> options) {
		if (options.containsKey(OPTION_NAME_MAXITER)) {
			try {
				
				return new FruchtermanReingoldLayout(
						PREFUSE_GRAPH, 
						(Integer)options.get(OPTION_NAME_MAXITER)
						);
				
			} catch (ClassCastException e) {
				throw GamaRuntimeException.error("Option "+OPTION_NAME_MAXITER+" of this layout is supposed to be an integer.");
			}
		} else {
			return new FruchtermanReingoldLayout(PREFUSE_GRAPH);
		}
		
	}

	@Override
	protected String getLayoutName() {
	
		return NAME;
	}
	

	@Override
	protected Collection<String> getLayoutOptions() {
		
		return new LinkedList<String>() {{
			add(OPTION_NAME_MAXITER);
		}};
		
	}

}
