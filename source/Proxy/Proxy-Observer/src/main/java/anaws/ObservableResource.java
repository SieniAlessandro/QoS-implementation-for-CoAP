package anaws;

import java.io.Serializable;
import java.net.InetSocketAddress;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.observe.ObservingEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;

import com.thoughtworks.xstream.XStream;

public class ObservableResource extends CoapResource implements Serializable {
	public enum SubjectState {
		UNVAVAILABLE, ONLY_CRITICAL, AVAILABLE
	}

	final private boolean DEBUG = true;
	private ProxyObserver server;
	private SubjectState state;
	private boolean negotiation;
	static private int seqnum = 0;

	public ObservableResource() {
		super("defaultName");
		this.setObservable(true);
		this.setVisible(true);
		this.state = SubjectState.ONLY_CRITICAL;
		this.negotiation = false;
	}

	public void setName(String name) {
		super.setName(name);
	}

	public void setServer(ProxyObserver server) {
		this.server = server;
	}

	@Override
	public String toString() {
		this.server = null;
		return new XStream().toXML(this);
	}

	public int getPriority(int priority) throws IllegalArgumentException {
		int dec;
		switch (priority) {
		case 0x000000:
			dec = 1;
			break;
		case 0x400000:
			dec = 2;
			break;
		case 0x800000:
			dec = 3;
			break;
		case 0xc00000:
			dec = 4;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return dec;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		if (state.equals(SubjectState.UNVAVAILABLE)) {
			System.out.println("Subject is unavailable");
			return;
		}

		int priority = getPriority(exchange.getRequestOptions().getObserve());
		// store observer information if the endpoint is not already present
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort();
		ObservingEndpoint observingEndpoint = null;
		// if there is a running negotation or a new endpoint
		if ( !server.isEndpointPresent(observerID) ) {
			// REGISTRATION PHASE
			if (DEBUG)
				System.out.println("\t[DEBUG] Observer " + observerID + " not present, added to the list ");
			observingEndpoint = new ObservingEndpoint(
					new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort()));
			server.addObserver(observerID, observingEndpoint);
			if (priority > 2 && state.equals(SubjectState.ONLY_CRITICAL)) {
				// NEGOTIATION
				Response response = new Response(CoAP.ResponseCode.NOT_ACCEPTABLE);
				response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, 0x400000)));
				if (DEBUG) {
					System.out.println("\t[DEBUG] Negotiation Started " + response.toString());
				}
				server.setNegotation(observerID, true);
				exchange.respond(response);
			} else {
				if (DEBUG) {
					System.out.println("\t[DEBUG] Accepting the request from " + exchange.getSourcePort() + " request: " + exchange.getRequestOptions().toString());
				}
				// Request accepted without negotiation
				finalizeObservation(exchange, observingEndpoint, observerID);
			}
		} else if ( server.getNegotationState(observerID) ) {
			// this was a negotiation so there is no need to respond
			if (DEBUG)
				System.out.println("\t[DEBUG] Negotiation ended ");
//			TODO controllo sulla conferma della proposta
			finalizeObservation(exchange, server.getObservingEndpoint(observerID), observerID);
		} else {
			Response response = new Response(CoAP.ResponseCode.CONTENT);
			response.setOptions( new OptionSet().setObserve(seqnum++));
			response.setPayload(fetchResource(this.getName()));
			exchange.respond(response);
			if (DEBUG)
				System.out.println("\t[DEBUG] Notification sent to : " + exchange.getSourcePort() + " notification: "+ response.getPayloadString());
		}

	}
	
	private void finalizeObservation(CoapExchange exchange, ObservingEndpoint observingEndpoint, String observerID) {
		Response response = new Response(CoAP.ResponseCode.CONTENT);
		response.setOptions(new OptionSet().addOption(
				new Option(OptionNumberRegistry.OBSERVE, exchange.getRequestOptions().getObserve())));
		response.setPayload(fetchResource(this.getName()));
		exchange.respond(response);
		ObserveRelation relation = new ObserveRelation(observingEndpoint, this, exchange.advanced());
		relation.setEstablished();
		server.setNegotation(observerID, false);
		this.addObserveRelation(relation);
	}

	private String fetchResource(String name) {
		return "RESOURCE VALUE #" + seqnum;
	}

//	TODO 
	
}
