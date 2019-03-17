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
	private boolean noNegotiation;

	public ObservableResource() {
		super("defaultName");
		this.setObservable(true);
		this.setVisible(true);
		this.state = SubjectState.AVAILABLE;
		this.noNegotiation = true;
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
		int priority = getPriority(exchange.getRequestOptions().getObserve());

		if (DEBUG) 
			System.out.println("\n[DEBUG] HandleGET> " + priority);
		if (state.equals(SubjectState.UNVAVAILABLE)) {
			System.out.println("Subject is unavailable");
			return;
		}

		// store observer information if the endpoint is not already present
		Endpoint observer = exchange.advanced().getEndpoint();
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort();
		ObservingEndpoint observingEndpoint = null;
		if (!server.isEndpointPresent(observerID)) {
			if (DEBUG) 
				System.out.println("\n[DEBUG] Observer not present, add it to the list ");
			server.addObserver(observerID, observer, observingEndpoint);
			observingEndpoint = new ObservingEndpoint(
					new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort()));
		} else 
			observingEndpoint = server.getObservingEndpoint(observerID);

		if (priority > 2 && state == SubjectState.ONLY_CRITICAL) {
			// NEGOTIATION
			Response response = new Response(CoAP.ResponseCode.NOT_ACCEPTABLE);
			response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, 0x400000)));
			response.setPayload(fetchResource(this.getName()));
			if (DEBUG) {
				System.out.println("\n[DEBUG] Negotiation Started ");
				System.out.println("\n[DEBUG] " + response.toString());
			}
			exchange.respond(response);
			noNegotiation = false;
		} else {
			if (!noNegotiation) {
				// Request accepted without negotiation
				Response response = new Response(CoAP.ResponseCode.CONTENT);
				response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, 0x400000)));
				response.setPayload(fetchResource(this.getName()));
				if (DEBUG) {
					System.out.println("\n[DEBUG] Accepting the request ");
					System.out.println("\n[DEBUG] " + response.toString());
				}
				exchange.respond(response);
			}
			// this was a negotiation so there is no need to respond
			ObserveRelation relation = new ObserveRelation(observingEndpoint, this, exchange.advanced());
			if (DEBUG) 
				System.out.println("\n[DEBUG] Building observe relation ");
			relation.setEstablished();
			relation.notifyObservers();
		}
	}

	private String fetchResource(String name) {
		return "RESOURCE VALUE";
	}

}
