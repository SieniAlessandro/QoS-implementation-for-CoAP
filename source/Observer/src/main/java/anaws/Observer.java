package anaws;

import java.util.*;

import javax.naming.directory.InvalidAttributesException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;

public class Observer {

	private String ipv6Proxy;
	private int portProxy;
	private CoapClient observerCoap;
	private HashSet<WebLink> resourceList;
	private static Scanner scanner;
	private HashMap<String, CoapObserveRelation> relations;

	public Observer(String ipv6Proxy, int portProxy) {
		this.ipv6Proxy = ipv6Proxy;
		this.portProxy = portProxy;
		this.observerCoap = new CoapClient();
		this.resourceList = new HashSet<WebLink>();
		this.relations = new HashMap<String, CoapObserveRelation>();
		resourceDiscovery();
	}

	public void resourceDiscovery() {
		System.out.print("Discovery...\n");
		resourceList.clear();
		resourceList.addAll(observerCoap.discover());
	}

	public int getQoSBits(int priority) throws IllegalArgumentException {
		int hex;
		switch (priority) {
		case 1:
			hex = 0x000000;
			break;
		case 2:
			hex = 0x400000;
			break;
		case 3:
			hex = 0x800000;
			break;
		case 4:
			hex = 0xc00000;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return hex;
	}

	public void resourceRegistration() {
		if (resourceList.isEmpty()) {
			System.out.println("No resource available, please run discovery first");
			return;
		}

		System.out.print("Resource Registration\n");
		System.out.print("Resource Name: ");
		String resourceName = scanner.next();
		System.out.print("Priority: ");
		int priority = getQoSBits(scanner.nextInt());

		Request observeRequest = new Request(Code.GET);
		try {
			// Set the priority level using the first 2 bits of the observe option value
			observeRequest.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, priority)));
		} catch (IllegalArgumentException ex) {
			System.out.println("Invalid Priority Level");
		}

		observeRequest.setURI("coap://[" + this.ipv6Proxy + "]:" + this.portProxy + "/" + resourceName);
		CoapResponse response = observerCoap.advanced(observeRequest);

		// Registration failed
		if (!response.getOptions().hasObserve()) {
			System.out.println("Subject rejected the observe request to the resource " + resourceName);
			return;
		}
		// Observation accepted then create a relation
		if (response.getOptions().getObserve() == priority) {
			System.out.println("Observe Relation accepted");
			System.out.println(response.getResponseText());
		} else {
			// Request not accepted but Subject wants to negotiate
			observeRequest.setOptions(new OptionSet()
					.addOption(new Option(OptionNumberRegistry.OBSERVE, response.getOptions().getObserve())));
			CoapObserveRelation observeRelation = observerCoap.observe(observeRequest, new CoapHandler() {

				public void onLoad(CoapResponse response) {
					System.out.println("Negotiation terminated, starting listen to notifications");
				}

				public void onError() {
					System.out.println("Negotiation failed!");
				}

			});

			observeRelation.setNotificationListener(new NotificationHandler());
			relations.put(resourceName, observeRelation);
		}
	}

	public void resourceCancellation() {
		System.out.print("Resource Cancellation\n");
		System.out.print("Resource Name: ");
		String resourceName = scanner.next();

		CoapObserveRelation relation = relations.get(resourceName);
		if (relation == null) {
			System.out.println("Observe relation on " + resourceName + " not found");
			return;
		}

		relation.reactiveCancel();
	}

	public void printHelpMenu() {
		String commandList = "1) Request the list of resources\n" + "2) Resource registration\n"
				+ "3) Resource cancellation\n" + "4) Print Help Menu\n" + "5) Exit\n";
		System.out.println("List of commands:\n" + commandList);
	}

	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		Observer observerClient = new Observer("::1", 6666);

		System.out.println("Welcome to the Observer's Command Line Interface");
		observerClient.printHelpMenu();
		while (true) {
			System.out.print("Observer> ");
			switch (scanner.nextInt()) {
			case 1:
				observerClient.resourceDiscovery();
				break;
			case 2:
				observerClient.resourceRegistration();
				break;
			case 3:
				observerClient.resourceCancellation();
				break;
			case 4:
				observerClient.printHelpMenu();
				break;
			case 5:
				System.out.println("Exiting... Good bye!");
				System.exit(0);
				break;
			default:
				continue;
			}
		}
	}
}
