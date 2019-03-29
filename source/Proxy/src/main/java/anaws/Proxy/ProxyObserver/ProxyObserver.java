package anaws.Proxy.ProxyObserver;

import java.util.HashMap;

import anaws.Proxy.ProxySubject.ProxySubject;
import anaws.Proxy.ProxySubject.Registration;
import anaws.Proxy.ProxySubject.SensorData;
import anaws.Proxy.ProxySubject.SensorNode;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.Resource;

import org.eclipse.californium.core.server.ServerState;

public class ProxyObserver {

	private CoapServer proxyObserver;
	
	private ProxySubject proxySubject;
	private Map<String, ObservableResource> resourceList;
	private Map<String, ObserverState> observers;
	private static Scanner scanner;

	private ArrayList<SensorNode> sensors;

	private boolean CLI;
	private boolean autocomplete;

	public ProxyObserver(boolean CLI, boolean autocomplete) {
		this.proxyObserver = new CoapServer();
		this.observers = new HashMap<String, ObserverState>();
		this.resourceList = new HashMap<String, ObservableResource>();
		this.sensors = new ArrayList<SensorNode>();
		this.CLI = CLI;
		this.autocomplete = autocomplete;

		System.out.println("\t[INFO] Starting listening...");
		proxyObserver.start();
	}
	
	public void addProxySubject(ProxySubject proxySubject ) {
		this.proxySubject = proxySubject;
	}

	public void setState(String sensorAddress, ServerState state) {
		for (ObservableResource o : resourceList.values()) {
			if (o.getPath().equals(sensorAddress)) {
				System.out.println("State of " + o.getURI() + " changed from " + o.getServerState() + " to " + state);
				o.setServerState(state);
			}
		}
	}

	public void addObserver(String key, ObserverState state) {
		observers.put(key, state);
	}

	public ObserverState getObserverState(String key) {
		return observers.get(key);
	}

	public boolean isObserverPresent(String key) {
		return observers.containsKey(key);
	}

	public void clearObservation(SensorNode sensor, String resourceName) {
		String key = "/" + sensor.toString() + "/" + resourceName;

		resourceList.get(key).clearAndNotifyObserveRelations(CoAP.ResponseCode.SERVICE_UNAVAILABLE);
	}

	public void clearAllObservation() {
		resourceList.values().forEach(r -> r.clearAndNotifyObserveRelations(CoAP.ResponseCode.SERVICE_UNAVAILABLE));
	}

	public void addResource(SensorNode sensor, String resourceName) {

		ObservableResource resource = new ObservableResource(resourceName, this);

		if (sensors.contains(sensor)) {
			// sensor already present
			for (Resource r : proxyObserver.getRoot().getChildren()) {
				if (r.getName().equals(sensor.toString()))
					r.add(resource);
			}
		} else {
			sensors.add(sensor);
			CoapResource sensorResource = new ObservableResource(sensor.toString(), this);
			sensorResource.setVisible(false);
			sensorResource.add(resource);
			proxyObserver.add(sensorResource);
		}

		resourceList.put(resource.getURI(), resource);
		System.out.println("Resource \"" + resource.getName() + "\" of sensor \"" + sensor.toString()
				+ "\" added to the resource list\n");
	}

	public void deleteResource(SensorNode sensor, String resourceName) {

		Resource sensorResource = null;
		// find sensor resource
		for (Resource sr : proxyObserver.getRoot().getChildren()) {
			if (sr.getName().equals(sensor.toString())) {
				sensorResource = sr;
				break;
			}
		}
		// remove the resource of that sensor
		for (Resource resource : sensorResource.getChildren()) {
			if (resource.getName().equals(resourceName)) {
				sensorResource.delete(resource);
				break;
			}
		}
		// check if the sensor has no resource then remove it
		if (sensorResource.getChildren().isEmpty()) {
			proxyObserver.remove(sensorResource);

			// da levare quando si userà la struttura del proxy subject
			for (SensorNode s : sensors) {
				if (s.toString().equals(sensorResource.getName())) {
					sensors.remove(s);
					break;
				}
			}
		}

		System.out.println("Resource \"" + resourceName + "\" of sensor \"" + sensor.toString()
				+ "\" removed from the resource list\n");
	}

	synchronized public void triggerChange(SensorData data) {
		String resourceName = data.getRegistration().getType();
		String sensor = data.getRegistration().getSensorNode().toString();
		boolean critical = data.getCritic();
		String key = "/" + sensor + "/" + resourceName;
		
		
		if (resourceList.get(key).getObserverCount() == 0) {
			System.out.println("No Observe Relations on this resource");
			return;
		}
		resourceList.get(key).setSensorData(data);

		if (!critical) {
			resourceList.get(key).changed();
		} else
			resourceList.get(key).changed(new CriticalRelationFilter());

		System.out.println("Current observers on this resource :" + resourceList.get(key).getObserverCount());
	}

	public void readResourcesFile() {

	}
	
	public SensorData requestValueCache(SensorNode sensor, String resourceName ) {	
		return proxySubject.getValue(sensor.toString(), resourceName);
	}
	
	public void requestRegistration(SensorNode sensor, String resourceName, boolean critical ) {
		proxySubject.newRegistration(sensor, resourceName, critical);
	}

	/*******************************
	 * COMMAND LINE TESTING FUNCTIONS
	 *******************************/

	private void printHelpMenuCLI() {
		String commandList = "1) Print Help Menu\n" + "2) Add a resource\n" + "3) Delete a resource\n"
				+ "4) Clear Observe Relations of a resource\n" + "5) Simulate a Resource Change \n"
				+ "6) Simulate a Critical Resource Change \n" + "7) Change Server State \n" + "8) Exit \n";
		System.out.println("List of commands:\n" + commandList);
	}

	private void changeStateCLI() {
		SensorNode sensor = null;
		if (!autocomplete) {
			System.out.print("Change State of a sensor node: \n");
			sensor = readSensorNetworkConfig();
		} else
			sensor = new SensorNode("::1", 5683);

		System.out.print("Change Server State: ( 1 - AVAILABLE, 2 - ONLY_CRITICAL, 3 - UNAVAILABLE )\n");
		int cmd = scanner.nextInt();
		try {
			switch (cmd) {
			case 1:
				setState("/" + sensor.toString() + "/", ServerState.AVAILABLE);
				break;
			case 2:
				setState("/" + sensor.toString() + "/", ServerState.ONLY_CRITICAL);
				break;
			case 3:
				setState("/" + sensor.toString() + "/", ServerState.UNVAVAILABLE);
				break;
			default:
				System.out.print("Invalid State\n");
				break;
			}
		} catch (InputMismatchException e) {
			System.out.println("Invalid input");
			scanner.nextLine();
		}
	}

	private SensorNode readSensorNetworkConfig() {
		SensorNode sensor = null;
		try {
			System.out.print("Sensor IPv6: \n");
			String ip = scanner.next();
			System.out.print("Sensor port: \n");
			String port = scanner.next();
			sensor = new SensorNode(ip, Integer.parseInt(port));
		} catch (InputMismatchException e) {
			System.out.println("Invalid input");
			scanner.nextLine();
		}
		return sensor;
	}

	private String readResourceName() {
		String resourceName = "";
		try {
			System.out.print("Resource name: \n");
			resourceName = scanner.next();
		} catch (InputMismatchException e) {
			System.out.println("Invalid input");
			scanner.nextLine();
		}
		return resourceName;
	}

	private void addResourceCLI() {
		SensorNode sensor = null;
		String resourceName = "";
		if (!autocomplete) {
			System.out.print("Add Resourse: \n");
			sensor = readSensorNetworkConfig();
			resourceName = readResourceName();
		} else {
			sensor = new SensorNode("::1", 5683);
			resourceName = "temperature";
		}

		addResource(sensor, resourceName);
	}

	private void deleteResourceCLI() {
		SensorNode sensor = null;
		String resourceName = "";
		if (!autocomplete) {
			System.out.print("Change value of the resource: \n");
			sensor = readSensorNetworkConfig();
			resourceName = readResourceName();
		} else {
			sensor = new SensorNode("::1", 5683);
			resourceName = "temperature";
		}

		deleteResource(sensor, resourceName);

	}

	private void triggerChangeCLI() {
		SensorNode sensor = null;
		String resourceName = "";
		if (!autocomplete) {
			System.out.print("Change value of the resource: \n");
			sensor = readSensorNetworkConfig();
			resourceName = readResourceName();
		} else {
			sensor = new SensorNode("::1", 5683);
			resourceName = "temperature";
		}
		
		SensorData data = new SensorData(new Registration(null, sensor, resourceName, false, null), Math.random() * 10 + 20, 60, false);
		
		triggerChange(data);
	}

	private void triggerCriticalChangeCLI() {
		SensorNode sensor = null;
		String resourceName = "";
		if (!autocomplete) {
			System.out.print("Change value of the resource: \n");
			sensor = readSensorNetworkConfig();
			resourceName = readResourceName();
		} else {
			sensor = new SensorNode("::1", 5683);
			resourceName = "temperature";
		}
		SensorData data = new SensorData(new Registration(null, sensor, resourceName, false, null), Math.random() * 10 + 30, 60, false);
		
		triggerChange(data);
	}

	private void clearObservationCLI() {
		SensorNode sensor = null;
		String resourceName = "";
		if (!autocomplete) {
			System.out.print("Change value of the resource: \n");
			sensor = readSensorNetworkConfig();
			resourceName = readResourceName();
		} else {
			sensor = new SensorNode("::1", 5683);
			resourceName = "temperature";
		}

		System.out.println("Clear relation: " + sensor.toString() + "/" + resourceName);
//		clearObservation("/" + sensor.toString() + "/" + resourceName);
	}

	public static void main(String[] args) {
		ProxyObserver server = new ProxyObserver(true, true);
		scanner = new Scanner(System.in);

		System.out.println("Welcome to the ProxyObserver Command Line Interface");
		server.printHelpMenuCLI();

		if (server.autocomplete)
			server.addResourceCLI();
		while (true) {
			try {
				System.out.print("ProxyObserver> ");
				switch (scanner.nextInt()) {
				case 1:
					server.printHelpMenuCLI();
					break;
				case 2:
					server.addResourceCLI();
					break;
				case 3:
					server.deleteResourceCLI();
					break;
				case 4:
					server.clearObservationCLI();
					break;
				case 5:
					server.triggerChangeCLI();
					break;
				case 6:
					server.triggerCriticalChangeCLI();
					break;
				case 7:
					server.changeStateCLI();
					break;
				case 8:
					System.out.println("Exiting... Good bye!");
					server.clearAllObservation();
					System.exit(0);
					break;
				default:
					continue;
				}
			} catch (InputMismatchException e) {
				System.out.println("Invalid command");
				scanner.nextLine();
			}
		}
	}

}

/*
 * TODO ProxySubject: - sicuramente non ti trova californium version
 * 2.0.0-SNAPSHOT, aggiungi il contenuto dello zip che ti ho mandato su telegram
 * nella directory: Mac: /Users/<user_name>/.m2/repository/org/eclipse -
 * aggiungere campo a SensorNode di tipo ServerState ( import
 * org.eclipse.californium.core.server.ServerState ). DOPO che il valore di
 * questo campo cambia chiamare la funzione
 * ProxyObserver.setSensorState(SensorNode sensor ), in modo da aggiornare il
 * campo ServerState delle risorse di quel sensore ( server nella registrazione
 * tra proxy e observer )
 * 
 */
