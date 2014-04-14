/*********************************************************************************************
 * 
 *
 * 'FIPAContractNet.java', in plugin 'msi.gaml.extensions.fipa', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.extensions.fipa;

/**
 * Implementation of the FIPA Contract Net interaction protocol. Reference :
 * http://www.fipa.org/specs/fipa00029/SC00029H.html
 */
public class FIPAContractNet extends FIPAProtocol {

	/** Definition of protocol model. */
	private static Object[] __after_cancel = {
			FIPAConstants.Performatives.FAILURE,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null,
			FIPAConstants.Performatives.INFORM,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null };

	/** The __after_accept. */
	private static Object[] __after_accept = {
			FIPAConstants.Performatives.FAILURE,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null,
			FIPAConstants.Performatives.INFORM,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null };

	/** The __after_propose. */
	private static Object[] __after_propose = {
			FIPAConstants.Performatives.FAILURE,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), INITIATOR, null,
			FIPAConstants.Performatives.CANCEL,
			Integer.valueOf(FIPAConstants.AGENT_ACTION_REQ), INITIATOR,
			__after_cancel, FIPAConstants.Performatives.ACCEPT_PROPOSAL,
			Integer.valueOf(FIPAConstants.AGENT_ACTION_REQ), INITIATOR,
			__after_accept, FIPAConstants.Performatives.REJECT_PROPOSAL,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), INITIATOR, null, };

	/** The __after_cfp. */
	private static Object[] __after_cfp = {
			FIPAConstants.Performatives.FAILURE,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null,
			FIPAConstants.Performatives.CANCEL,
			Integer.valueOf(FIPAConstants.AGENT_ACTION_REQ), INITIATOR,
			__after_cancel, FIPAConstants.Performatives.REFUSE,
			Integer.valueOf(FIPAConstants.CONVERSATION_END), PARTICIPANT, null,
			FIPAConstants.Performatives.PROPOSE,
			Integer.valueOf(FIPAConstants.AGENT_ACTION_REQ), PARTICIPANT,
			__after_propose };

	/** The roots. */
	public static Object[] roots = { FIPAConstants.Performatives.CFP,
			Integer.valueOf(FIPAConstants.AGENT_ACTION_REQ), INITIATOR, __after_cfp };

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.misc.current_development.FIPAProtocol#getName()
	 */
	@Override
	public int getIndex() {
		return FIPAConstants.Protocols.FIPA_CONTRACT_NET;
	}

	@Override
	public String getName() {
		return FIPAConstants.Protocols.FIPA_CONTRACT_NET_STR;
	}

}
