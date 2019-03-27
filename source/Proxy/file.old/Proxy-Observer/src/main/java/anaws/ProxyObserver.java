package anaws;

import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.eclipse.californium.core.server.ServerState;

public class ProxyObserver {

	final private static boolean CLI = false;

	private CoapServer proxyObserver;
	private Map<String, ObservableResource> resourceList;
	private Map<String, ObserverState> observers;
	private static Scanner scanner;
	private ArrayList<String> subjects;

	public ProxyObserver() {
		proxyObserver = new CoapServer();
		observers = new HashMap<String, ObserverState>();
		resourceList = new HashMap<String, ObservableResource>();
		subjects = new ArrayList<String>();
		System.out.println("Starting listening...");
		proxyObserver.start();

//		readResourcesFile();
	}

	public void init() {
//		Collection<Resource> resources = readResourcesFile();
//		if (resources == null)
//			return;
//		System.out.println(resources.toString());
//		addAllResources(resources);
	}

	public void setState(String subjectAddress, ServerState state) {
		for (ObservableResource o : resourceList.values()) {
			if (o.getPath().equals(subjectAddress)) {
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

	public void clearObservation(String resourceName) {
		resourceList.get(resourceName).clearAndNotifyObserveRelations(null);
	}

	public void addResource(String subjectAddress, ObservableResource resource) {

		if (subjects.contains(subjectAddress)) {
			// subject already present
			for (Resource r : proxyObserver.getRoot().getChildren()) {
				if (r.getName().equals(subjectAddress))
					r.add(resource);
			}
		} else {
			subjects.add(subjectAddress);
			CoapResource subject = new ObservableResource();
			subject.setName(subjectAddress);
			subject.setVisible(false);
			subject.add(resource);
			proxyObserver.add(subject);
		}

		resourceList.put(resource.getURI(), resource);
		System.out.println("Resource \"" + resource.getName() + "\" of sensor \"" + resource.getPath()
				+ "\" added to the resource list\n");

//		updateResourcesFile(subjectAddress);
	}

	public void triggerChange(String resourceName, double value, boolean critical) {
		if (resourceList.get(resourceName).getObserverCount() == 0) {
			System.out.println("No Observe Relations on this resource");
			return;
		}
		if (!critical) {
			resourceList.get(resourceName).setResourceValue(value);
//		resourceList.get(resourceName).setResourceValue(proxySubject.fetchResource(resourceName));
			resourceList.get(resourceName).changed();
		} else {
			resourceList.get(resourceName).setResourceValue(value);
			resourceList.get(resourceName).changed(new CriticalRelationFilter());
		}
		System.out.println("Current observers on this resource :" + resourceList.get(resourceName).getObserverCount());
	}

	public void updateResourcesFile(String subjectAddress) {
		Resource updated = null;
		for (Resource r : proxyObserver.getRoot().getChildren()) {
			if (r.getName().equals(subjectAddress))
				updated = r;
		}

		String pathname = "Local/Node_" + subjects.size() + ".xml";

//		try (FileOutputStream fout = new FileOutputStream(new File(pathname));
//				ObjectOutputStream bout = new ObjectOutputStream(fout)) {
//			bout.writeObject(updated);
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ObservableResource.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			File file = new File(pathname);

			// Write XML to StringWriter
			jaxbMarshaller.marshal(updated, file );

			// Verify XML Content
			String xmlContent = file.toString();
			System.out.println(xmlContent);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public void readResourcesFile() {
		Resource subject = null;
		String pathname = "Local/Node_1.bin";

		try (FileInputStream fin = new FileInputStream(new File(pathname));
				ObjectInputStream bin = new ObjectInputStream(fin)) {
			subject = (Resource) bin.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			System.err.println(ex.getMessage());
		}

		if (subject != null)
			proxyObserver.add(subject);
	}

	/*******************************
	 * COMMAND LINE TESTING FUNCTIONS
	 *******************************/

	private void printHelpMenuCLI() {
		String commandList = "1) Start the server\n" + "2) Add a resource\n" + "3) Delete a resource\n"
				+ "4) Print Help Menu\n" + "5) Clear Observe Relations of a resource\n"
				+ "6) Simulate a Resource Change \n" + "7) Simulate a Critical Resource Change \n"
				+ "8) Change Server State \n" + "9) Exit \n";
		System.out.println("List of commands:\n" + commandList);
	}

	private void changeStateCLI() {
		String subjectAddress = "";
		if (CLI) {
			System.out.print("Subject IPv6:port\n");
			try {
				subjectAddress = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else {
			subjectAddress = "::1:5683";

		}

		System.out.print("Change Server State: ( 1 - AVAILABLE, 2 - ONLY_CRITICAL, 3 - UNAVAILABLE )\n");
		int cmd = scanner.nextInt();
		try {
			switch (cmd) {
			case 1:
				setState("/" + subjectAddress + "/", ServerState.AVAILABLE);
				break;
			case 2:
				setState("/" + subjectAddress + "/", ServerState.ONLY_CRITICAL);
				break;
			case 3:
				setState("/" + subjectAddress + "/", ServerState.UNVAVAILABLE);
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

	private void addResourceCLI() {
		String subjectAddress = "";
		String resourceName = "";
		if (CLI) {
			try {
				System.out.print("Add Resourse\n");
				System.out.print("Subject IPv6:port\n");
				subjectAddress = scanner.next();
				System.out.print("Resource Name: ");
				resourceName = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else {
			subjectAddress = "::1:5683";
			resourceName = "temperature";
		}

		ObservableResource or = new ObservableResource();
		or.setName(resourceName);
		or.setServer(this);
		addResource(subjectAddress, or);
	}

	private void deleteResourceCLI() {
		System.out.println("work in progress...");
	}

	private void triggerChangeCLI() {
		String subjectAddress = "";
		String resourceName = "";
		if (CLI) {
			try {
				System.out.print("Add Resource\n");
				System.out.print("Subject Address <IPv6:port>\n");
				subjectAddress = scanner.next();
				System.out.print("Resource Name: ");
				resourceName = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else {
			subjectAddress = "::1:5683";
			resourceName = "temperature";
		}
		triggerChange("/" + subjectAddress + "/" + resourceName, Math.random() * 10 + 20, false);
	}

	private void triggerCriticalChangeCLI() {
		String subjectAddress = "";
		String resourceName = "";
		if (CLI) {
			try {
				System.out.print("Add Resource\n");
				System.out.print("Subject Address <IPv6:port>\n");
				subjectAddress = scanner.next();
				System.out.print("Resource Name: ");
				resourceName = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else {
			subjectAddress = "::1:5683";
			resourceName = "temperature";
		}
		triggerChange("/" + subjectAddress + "/" + resourceName, Math.random() * 10 + 30, true);
	}

	private void clearObservationCLI() {
		String subjectAddress = "";
		String resourceName = "";
		if (CLI) {
			try {
				System.out.print("Clear Resource\n");
				System.out.print("Subject Address <IPv6:port>\n");
				subjectAddress = scanner.next();
				System.out.print("Resource Name: ");
				resourceName = scanner.next();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
		} else {
			subjectAddress = "::1:5683";
			resourceName = "temperature";
		}
		clearObservation("/" + subjectAddress + "/" + resourceName);
	}

	public static void main(String[] args) {
		ProxyObserver server = new ProxyObserver();
		scanner = new Scanner(System.in);

		System.out.println("Welcome to the ProxyObserver Command Line Interface");
		server.printHelpMenuCLI();
		server.addResourceCLI();
		while (true) {
			try {
				System.out.print("ProxyObserver> ");
				switch (scanner.nextInt()) {
				case 1:
					server.init();
					break;
				case 2:
					server.addResourceCLI();
					break;
				case 3:
					server.deleteResourceCLI();
					break;
				case 4:
					server.printHelpMenuCLI();
					break;
				case 5:
					server.clearObservationCLI();
				case 6:
					server.triggerChangeCLI();
					break;
				case 7:
					server.triggerCriticalChangeCLI();
					break;
				case 8:
					server.changeStateCLI();
					break;
				case 9:
					System.out.println("Exiting... Good bye!");
					server.clearObservationCLI();
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
