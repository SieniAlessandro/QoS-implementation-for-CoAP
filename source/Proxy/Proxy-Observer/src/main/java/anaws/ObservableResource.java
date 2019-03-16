package anaws;

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

public class ObservableResource extends CoapResource {
	public enum SubjectState {
		UNVAVAILABLE, ONLY_CRITICAL, AVAILABLE
	}

	private ProxyObserver server;
	private SubjectState state;
	private int priority;

	public ObservableResource(String name, ProxyObserver server) {
		super(name);

		this.server = server;
		this.setObservable(true);
		this.setVisible(true);
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

		// store observer information if the endpoint is not already present
		Endpoint observer = exchange.advanced().getEndpoint();
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort();
		ObservingEndpoint observingEndpoint = null;
		if (!server.isEndpointPresent(observerID)) {
			server.addObserver(observerID, observer, observingEndpoint);
			observingEndpoint = new ObservingEndpoint(
					new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort()));
		}
		priority = getPriority(exchange.getRequestOptions().getObserve());

		if (state.equals(SubjectState.AVAILABLE)) {

			// respond to the observe request with the same value of QoS field ( priority
			// level ) and the first payload as notification
			Response response = new Response(CoAP.ResponseCode.CONTENT);
			response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, priority)));
			response.setPayload(fetchResource(this.getName()));
			exchange.respond(response);

			ObserveRelation relation = new ObserveRelation(observingEndpoint, this, exchange.advanced());
		} else {
			// NEGOTIATION 
		}
	}

	private String fetchResource(String name) {
		return "RESOURCE VALUE";
	}

}
