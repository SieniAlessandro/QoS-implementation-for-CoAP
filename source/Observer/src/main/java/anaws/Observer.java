package anaws;

import java.sql.Timestamp;
import java.util.*;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;

public class Observer {

	final private boolean DEBUG = true;

	private static Scanner scanner;

	private int portProxy;
	private int id;
	private int requestedPriority;
	private boolean CLI;
	private boolean autocomplete;

	private String ipv6Proxy;

	private CoapClient observerCoap;
	private ArrayList<WebLink> resourceList;
	private HashMap<String, CoapObserveRelation> relations;

	public Observer(String ipv6Proxy, int portProxy, int port) {
		this(ipv6Proxy, portProxy, port, false, true);
	}

	public Observer(String ipv6Proxy, int portProxy, int port, boolean CLI, boolean autocomplete) {
		this.observerCoap = new CoapClient();
		this.ipv6Proxy = ipv6Proxy;
		this.portProxy = portProxy;
		this.CLI = CLI;
		this.autocomplete = autocomplete;

		this.resourceList = new ArrayList<WebLink>();
		this.relations = new HashMap<String, CoapObserveRelation>();
		this.observerCoap.setURI("coap://[" + this.ipv6Proxy + "]:" + this.portProxy);

		this.id = port;

		CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
		builder.setPort(port);
		builder.setNetworkConfig(NetworkConfig.getStandard());
		observerCoap.setEndpoint(builder.build());

		resourceDiscovery();
	}

	public int getId() {
		return id;
	}

	public boolean isCLI() {
		return CLI;
	}

	public int getRequestedPriority() {
		return requestedPriority;
	}

	public void setRequestedPriority(int requestedPriority) {
		this.requestedPriority = getPriority(requestedPriority);
	}

	public HashMap<String, CoapObserveRelation> getRelations() {
		return relations;
	}

	public CoapClient getCoapClient() {
		return observerCoap;
	}

	private int getQoSBits(int priority) throws IllegalArgumentException {
		int hex;
		switch (priority) {
		case 1:
			hex = CoAP.QoSLevel.NON_CRITICAL_LOW_PRIORITY;
			break;
		case 2:
			hex = CoAP.QoSLevel.NON_CRITICAL_MEDIUM_PRIORITY;
			break;
		case 3:
			hex = CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY;
			break;
		case 4:
			hex = CoAP.QoSLevel.CRITICAL_HIGHEST_PRIORITY;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return hex;
	}

	private int getPriority(int priority) throws IllegalArgumentException {
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

	private void resourceRegistration(String resourceName, int priority, String path) {
		Request observeRequest = new Request(Code.GET);
		try {
			// Set the priority level using the first 2 bits of the observe option value
			observeRequest.setObserve();
			observeRequest.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, priority)));
		} catch (IllegalArgumentException ex) {
			System.out.println("Invalid Priority Level");
		}

		String URI = "coap://[" + this.ipv6Proxy + "]:" + this.portProxy + path;
		observeRequest.setURI(URI);
		if (DEBUG)
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")] Observer #" + id
					+ ">\t[DEBUG] Send Observe request: " + observeRequest.toString());
		CoapObserveRelation relation = observerCoap.observeAndWait(observeRequest,
				new ResponseHandler(this, priority, resourceName, URI, true));

		if (relation.isCanceled()) {
			if (DEBUG)
				System.out.println("Observer #" + id + ">\t[DEBUG] Relation is canceled");
		} else
			relations.put(resourceName, relation);
	}

	public void resourceDiscovery() {
		System.out.print("Discovery...\n");
		Set<WebLink> weblinks = observerCoap.discover();
		resourceList.clear();
		resourceList.addAll(weblinks);
		System.out.println(resourceList.toString());

	}

	private void resourceCancellation(String resourceName) {

		CoapObserveRelation relation = relations.get(resourceName);
		if (relation == null) {
			System.out.println("Observe relation on " + resourceName + " not found");
			return;
		}

		relation.proactiveCancel();
	}

	private String getRandomURI() {
		WebLink randomURI = new WebLink("well-known"); 
		while (randomURI.getURI().contains("well-known") || randomURI.getURI().contains("battery"))
			randomURI = resourceList.get((int) Math.floor(Math.random() * resourceList.size()));
		return randomURI.getURI();
	}

	private String[] splitURI(String uri) {
		String[] splitted = uri.split("/");
		return splitted;
	}

	/*******************************
	 * COMMAND LINE TESTING FUNCTIONS
	 *******************************/

	public void resourceRegistrationCLI() {
		if (resourceList.isEmpty()) {
			System.out.println("No resource available, please run discovery first");
			return;
		}

		requestedPriority = (int) Math.floor(Math.random() * 4 + 1);
		String subjectAddress = "";
		String resourceName = "";
		int priority = 1;

		if (!autocomplete) {
			try {
				System.out.print("Requesting an Observe Relations\nSelect a resource:\n");
				for (int i = 0; i < resourceList.size(); i++) {
					System.out.println(String.valueOf(i + 1) + ") " + resourceList.get(i));
				}
				int index = scanner.nextInt();
				index -= 1;
				String[] splitted = splitURI(resourceList.get(index).getURI());
				subjectAddress = splitted[1];
				resourceName = splitted[2];
				System.out.print("Priority: ");
				priority = getQoSBits(scanner.nextInt());
			} catch (InputMismatchException e) {
				System.out.println("Invalid Input");
				scanner.nextLine();
			}
		} else {
			String[] splitted = splitURI(getRandomURI());
			subjectAddress = splitted[1];
			resourceName = splitted[2];
			priority = getQoSBits(requestedPriority);
		}
		String path = "/" + subjectAddress + "/" + resourceName;

		if (!resourceList.toString().contains(path)) {
			System.out.println("Resource not found: " + path);
			return;
		}
		if (DEBUG)
			System.out.println("Resource found: " + path + " is present into " + resourceList.toString());

		resourceRegistration(resourceName, priority, path);
	}

	public void resourceCancellationCLI() {
		String resourceName = "";

		if (!autocomplete) {
			try {
				System.out.print("Resource Cancellation\n");
				System.out.print("Resource Name: ");
				resourceName = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else
			resourceName = "temperature";

		resourceCancellation(resourceName);
	}

	public void printHelpMenu() {
		String commandList = "1) Print Help Menu\n" + "2) Resource registration\n" + "3) Resource cancellation\n"
				+ "4) Request the list of resources\n" + "5) Exit\n";
		System.out.println("List of commands:\n" + commandList);
	}

	public void clearRelations() {
		System.out.println("Clearing observation...");
		relations.values().forEach(r -> r.proactiveCancel());
	}

	public void exit() {
		clearRelations();
		System.out.println("Exiting... Good bye!");
		System.exit(0);
	}

	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		boolean CLI = Boolean.parseBoolean(args[3]);
		boolean autocomplete = Boolean.parseBoolean(args[4]);
		Observer observerClient = new Observer(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), CLI,
				autocomplete);

		System.out.println("Welcome to the Observer's Command Line Interface");
		observerClient.printHelpMenu();
		if (observerClient.isCLI()) {
			if (observerClient.autocomplete)
				observerClient.resourceRegistrationCLI();
			while (true) {
				try {
					System.out.print("Observer # " + Integer.parseInt(args[2]) + "> ");
					switch (scanner.nextInt()) {
					case 1:
						observerClient.printHelpMenu();
						break;
					case 2:
						observerClient.resourceRegistrationCLI();
						break;
					case 3:
						observerClient.resourceCancellationCLI();
						break;
					case 4:
						observerClient.resourceDiscovery();
						break;
					case 5:
						observerClient.exit();
						break;
					default:
						continue;
					}
				} catch (InputMismatchException e) {
					System.out.println("Invalid command");
					scanner.nextLine();
				}
			}
		} else {
			observerClient.resourceRegistrationCLI();
		}
	}

}
