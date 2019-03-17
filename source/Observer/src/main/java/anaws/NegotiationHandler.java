package anaws;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;

public class NegotiationHandler implements CoapHandler {

	final private boolean DEBUG = true;
	private int priority;
	private Observer observer;
	private String resourceName;

	public NegotiationHandler(Observer observer, int priority, String resourceName) {
		this.observer = observer;
		this.priority = priority;
		this.resourceName = resourceName;
	}

	public void onLoad(CoapResponse response) {
		int responsePriority = response.getOptions().getObserve();
		// Registration failed because response doesn't have the observe option
		if (response == null || !response.getOptions().hasObserve()) {
			if (DEBUG) {
				System.out.println("\n[DEBUG] Request rejected ");
				System.out.println("\n[DEBUG] " + response.toString());
			}
//			System.out.println("Subject rejected the observe request to the resource ");
			onError();
			return;
		}

		// Response with the same QoS Field then change the handler to
		// NotificationHandler
		System.out.println(responsePriority);
		System.out.println(priority);

		if (responsePriority == priority) {
			if (DEBUG) {
				System.out.println("\n[DEBUG] Observe Relation accepted ");
				System.out.println("\n[DEBUG] " + response.toString());

			}
			observer.getRelations().get(resourceName).setNotificationListener(new NotificationHandler());
			return;
		} else {
			// Response with a different QoS Field accepted by default if the observer
			// doesn't explicit it
//			Scanner scanner = new Scanner(System.in);
//			System.out.println("Subject can't handle level " + priority + " request, it proposes level "
//					+ responsePriority + ". Do you accept? (y/n, default yes)");
//			switch (scanner.next()) {
//			case "n":
//				onError();
//				break;
//			default:
//				Request observeRequest = new Request(Code.GET);
//				observeRequest.setOptions(
//						new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, responsePriority)));
//				observer.getCoapClient().advanced(observeRequest);
//				observer.getRelations().get(resourceName).setNotificationListener(new NotificationHandler());
//				break;
//			}

			Request observeRequest = new Request(Code.GET);
			observeRequest
					.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, responsePriority)));
			if (DEBUG) {
				System.out.println("\n[DEBUG] Accepting the subject proposal" + observeRequest.toString());
				System.out.println("\n[DEBUG] " + observeRequest.toString());
			}
			observer.getCoapClient().advanced(observeRequest);
		}
	}

	public void onError() {
		observer.getRelations().remove(resourceName);
	}

}