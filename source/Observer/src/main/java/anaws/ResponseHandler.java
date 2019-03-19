package anaws;

import java.sql.Timestamp;
import java.util.Scanner;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
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

	public ResponseHandler(Observer observer, int priority, String resourceName, String URI) {
		this.observer = observer;
		this.priority = priority;
		this.resourceName = resourceName;
		this.URI = URI;
	}

	public void onLoad(CoapResponse response) {
		// Registration failed because response doesn't have the observe option
		if (response == null || !response.getOptions().hasObserve()) {
			if (DEBUG) {
				System.out.println("\t[DEBUG] Request rejected ");
				System.out.println("\t[DEBUG] " + response.toString());
			}
//			System.out.println("Subject rejected the observe request to the resource ");
			onError();
			return;
		}

		int responsePriority = response.getOptions().getObserve();
		// First notification after the observe request was accepted or normal
		// notification
		if (response.getCode().equals(CoAP.ResponseCode.CONTENT)) {
			// Observe relation accepted without negotiation or a notification arrived
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")] Notification Arrived: "
					+ response.getResponseText());
			return;
		} else if (response.getCode().equals(CoAP.ResponseCode.NOT_ACCEPTABLE)) {
			if (DEBUG) 
				System.out.println("\t[DEBUG] Nogotiation started, subject proposes " + response.getOptions());
			// Subject started the negotation, observer need to accept it
//			System.out.println("Subject can't handle level " + priority + " request, it proposes level "
//					+ responsePriority + ". Do you accept? (y/n, default yes)");
//			switch (scanner.nextLine()) {
//			case "n":
//				onError();
//				break;
//			default:
			Request observeRequest = new Request(Code.GET);
			observeRequest
					.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, responsePriority)));
			observeRequest.setURI(URI);
			if (DEBUG) 
				System.out.println("\t[DEBUG] Accepting the subject's proposal " + URI);
			observer.getCoapClient().advanced(observeRequest);
//				break;
//			}
		}
	}

	public void onError() {
		observer.getRelations().remove(resourceName);
	}

}