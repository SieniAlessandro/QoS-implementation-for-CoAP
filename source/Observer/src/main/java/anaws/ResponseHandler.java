package anaws;

import java.sql.Timestamp;
import java.util.Scanner;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;

public class ResponseHandler implements CoapHandler {

	final private boolean DEBUG = true;
	private int priority;
	private Observer observer;
	private String resourceName;
	private String URI;
	private boolean acceptProposal;

	public ResponseHandler(Observer observer, int priority, String resourceName, String URI, boolean acceptProposal) {
		this.observer = observer;
		this.priority = priority;
		this.resourceName = resourceName;
		this.URI = URI;
		this.acceptProposal = acceptProposal;
	}

	public void onLoad(CoapResponse response) {
		if (DEBUG)
			System.out.println("---------------------------------------");

		// Registration failed because response doesn't have the observe option
		if ( response == null ) {
			onError();
			return;
		}

		if ( response.getCode().equals(CoAP.ResponseCode.SERVICE_UNAVAILABLE) ) {
			Log.error("Observer " + observer.getId(), "Observe Relation interrupted by the server, responce code: " + response.getCode());
			onError();
			return;
		}

		if ( !response.getOptions().hasObserve() ) {
			Log.error("Observer " + observer.getId(), "No observe option found ");
			onError();
			return;
		}

		int responsePriority = response.getOptions().getObserve();
		// First notification after the observe request was accepted or normal
		// notification
		if (response.getCode().equals(CoAP.ResponseCode.CONTENT)) {
			// Observe relation accepted without negotiation or a notification arrived
			Log.info("Observer " + observer.getId(), "Notification Arrived: "
					+ response.advanced().toString());
			return;
		} else if (response.getCode().equals(CoAP.ResponseCode.NOT_ACCEPTABLE) && acceptProposal) {
			Log.info("Observer " + observer.getId(), "Negotiation started, subject proposes " + response.getOptions());
			// Subject started the negotiation, observer need to accept it
			Request observeRequest = new Request(Code.GET);
			observeRequest.setObserve();
			observeRequest
					.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, responsePriority)));
			observeRequest.setURI(URI);
			observer.setRequestedPriority(responsePriority);
			Log.info("Observer " + observer.getId(), "Accepting the subject's proposal " + observeRequest.toString());
			CoapObserveRelation relation = observer.getCoapClient().observe(observeRequest,
					new ResponseHandler(observer, priority, resourceName, URI, acceptProposal));
			if ( relation != null && !relation.isCanceled() ) {
				observer.getRelations().put(resourceName, relation);
			}
		}
	}

	public void onError() {
		observer.getRelations().remove(resourceName);
	}

}
