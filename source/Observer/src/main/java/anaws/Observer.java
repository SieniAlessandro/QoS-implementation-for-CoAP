package anaws;

import java.net.InetSocketAddress;
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


public class Observer {

	private String ipv6Proxy;
	private int portProxy;
	private static int instanceCount = 0;
	private int id;
	private int requestedPriority;
	private CoapClient observerCoap;
	private HashSet<WebLink> resourceList;
	private static Scanner scanner;
	private HashMap<String, CoapObserveRelation> relations;
	final private boolean DEBUG = true;

	public Observer(String ipv6Proxy, int portProxy) {
		this.ipv6Proxy = ipv6Proxy;
		this.portProxy = portProxy;
		this.observerCoap = new CoapClient();
		this.instanceCount++;
		this.id = instanceCount;
		this.resourceList = new HashSet<WebLink>();
		this.relations = new HashMap<String, CoapObserveRelation>();
		this.observerCoap.setURI("coap://[" + this.ipv6Proxy + "]:" + this.portProxy);
		resourceDiscovery();
	}
	
	public int getId() {
		return id;
	}

	public void resourceDiscovery() {
		System.out.print("Discovery...\n");
		resourceList.clear();
		resourceList.addAll(observerCoap.discover());
		System.out.println(resourceList.toString());
	}

	public int getQoSBits(int priority) throws IllegalArgumentException {
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
	
	public HashMap<String, CoapObserveRelation> getRelations() {
		return relations;
	}
	
	public CoapClient getCoapClient() {
		return observerCoap;
	}
	

	public void resourceRegistration() {
		if (resourceList.isEmpty()) {
			System.out.println("No resource available, please run discovery first");
			return;
		}

		System.out.print("Requesting an Observe Relations\n");
//		System.out.print("Subject IPv6:port\n");
		String subjectAddress = "::1:5683";//scanner.next();
//		System.out.print("Resource Name: ");
		String resourceName = "temperature" ;//scanner.next();
		String path = "/" + subjectAddress + "/" + resourceName;
		
		if ( !resourceList.toString().contains(path) ) {
			System.out.println("Resource not found: " + path);
			return;
		}
		if (DEBUG)
			System.out.println("Resource found: " + path + " is present into " + resourceList.toString());
		
		System.out.print("Priority: ");
		requestedPriority = (int)Math.floor(Math.random()*4+1);
		int priority = 	getQoSBits(requestedPriority);	// getQoSBits(scanner.nextInt());
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
			System.out.println("Observer #" + id + ">\t[DEBUG] Send Observe request: " + observeRequest.toString());
		CoapObserveRelation relation = observerCoap.observeAndWait( observeRequest, new ResponseHandler( this, priority, resourceName, URI, true) );

		if ( relation.isCanceled() ) {
			if (DEBUG)
				System.out.println("Observer #" + id + ">\t[DEBUG] Relation is canceled");
		} else 
			relations.put(resourceName, relation);
		
	}

	public int getRequestedPriority() {
		return requestedPriority;
	}

	public void setRequestedPriority(int requestedPriority) {
		this.requestedPriority = getPriority(requestedPriority);
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

		relation.proactiveCancel();
	}

	public void printHelpMenu() {
		String commandList = "1) Request the list of resources\n" + "2) Resource registration\n"
				+ "3) Resource cancellation\n" + "4) Print Help Menu\n" + "5) Exit\n";
		System.out.println("List of commands:\n" + commandList);
	}
	
	public InetSocketAddress getObserverAddress() {
		return observerCoap.getEndpoint().getAddress();
	}
	
	public void getObserverPort() {
		System.out.println(observerCoap.getEndpoint().toString() );
	}

	
	public void clearRelations() {
		relations.values().forEach(r -> r.proactiveCancel());
	}
	
	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		Observer observerClient = new Observer("::1", 5683);
			
		System.out.println("Welcome to the Observer's Command Line Interface");
		observerClient.printHelpMenu();
		observerClient.resourceRegistration();
		while (true) {
//			System.out.print("Observer> ");
//			switch (scanner.nextInt()) {
//			case 1:
//				observerClient.resourceDiscovery();
//				break;
//			case 2:
//				observerClient.resourceRegistration();
//				break;
//			case 3:
//				observerClient.resourceCancellation();
//				break;
//			case 4:
//				observerClient.printHelpMenu();
//				break;
//			case 5:
//				System.out.println("Exiting... Good bye!");
//				System.exit(0);
//				break;
//			default:
//				continue;
//			}
		}
	}

}
