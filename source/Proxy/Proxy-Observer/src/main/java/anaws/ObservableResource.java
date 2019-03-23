package anaws;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.ConcurrentCoapResource;
import org.eclipse.californium.core.server.ServerState;

import com.thoughtworks.xstream.XStream;

public class ObservableResource extends ConcurrentCoapResource {

	final private boolean DEBUG = true;
	final private int PROPOSAL = CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY;
	private int maxAge = 60;
	private ProxyObserver server;
	private double resourceValue;
	
	public double getResourceValue() {
		return resourceValue;
	}

	public void setResourceValue(double resourceValue) {
		this.resourceValue = resourceValue;
	}


	public ObservableResource() {
		super("default_name", 3);
		super.serverState = ServerState.AVAILABLE;
		this.setObservable(true);
		this.setObserveType(Type.CON);
		this.setVisible(true);
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
		case CoAP.QoSLevel.NON_CRITICAL_LOW_PRIORITY:
			dec = 1;
			break;
		case CoAP.QoSLevel.NON_CRITICAL_MEDIUM_PRIORITY:
			dec = 2;
			break;
		case CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY:
			dec = 3;
			break;
		case CoAP.QoSLevel.CRITICAL_HIGHEST_PRIORITY:
			dec = 4;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return dec;
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		System.out.println("---------------------------------------");
		if (DEBUG)
			System.out.println("\t[DEBUG] handleGET request with priority: " + getPriority(exchange.advanced().getCurrentRequest().getOptions().getObserve()));
		if (super.serverState.equals(ServerState.UNVAVAILABLE)) {
			System.out.println("Subject is unavailable");
			return;
		}
		int priority = getPriority(exchange.getRequestOptions().getObserve());
		// store observer information if the endpoint is not already present
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort();
		int mid = exchange.advanced().getRequest().getMID();
		if (!server.isObserverPresent(observerID)) {
			if (DEBUG)
				System.out.println("\t[DEBUG] Observer " + observerID + " not present, added to the list ");
			server.addObserver(observerID, new ObserverState(mid, false));
			handleRegistration(priority, observerID, exchange);
			return;
		}

		if (mid == server.getObserverState(observerID).getOriginalMID()) {
			// This is a notification because the exchange has the same MID of the original
			// request
			if (DEBUG)
				System.out.println("\t[DEBUG] Resource changed");
			sendNotification(exchange);
		} else {
			// The observer is already present but this is not a notification then it is a
			// request of reregistration
			handleRegistration(priority, observerID, exchange);
		}
	}

	private void handleRegistration(int priority, String observerID, CoapExchange exchange) {
		// Registration phase
		if (!server.getObserverState(observerID).isNegotiationState()) {
			if (priority < 3 && super.serverState.equals(ServerState.ONLY_CRITICAL)) {
				// First part of the negotiation, where subject make its proposal
				Response response = new Response(CoAP.ResponseCode.NOT_ACCEPTABLE);
				response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, PROPOSAL)));
				server.getObserverState(observerID).setNegotiationState(true);
				exchange.respond(response);
				exchange.advanced().getRelation().cancel();
				if (DEBUG) {
					System.out.println("\t[DEBUG] Negotiation Started: " + response.toString());
				}
			} else {
				if (DEBUG) {
					System.out.println("\t[DEBUG] Accepting the request from " + exchange.getSourcePort()
							+ " request without negotiation: " + exchange.getRequestOptions().toString());
				}
				// Request accepted without negotiation
				sendNotification(exchange);
			}
		} else {
			// This is the second part of a negotiation
			server.getObserverState(observerID).setNegotiationState(false);
			server.getObserverState(observerID).setOriginalMID(exchange.advanced().getRequest().getMID());
			sendNotification(exchange);
			if (DEBUG)
				System.out.println("\t[DEBUG] Negotiation ended ");
		}
	}

	private void sendNotification(CoapExchange exchange) {
		String value = "Value: " + resourceValue;
		exchange.setMaxAge(maxAge);
		exchange.respond(value);
		if (DEBUG)
			System.out.println(
					"\t[DEBUG] Notification sent to : " + exchange.getSourcePort() + " notification: " + value);
	}
}
