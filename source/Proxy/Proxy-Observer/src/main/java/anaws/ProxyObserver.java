package anaws;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.core.server.ServerState;

public class ProxyObserver {

	private CoapServer proxyObserver;
	private ArrayList<ObservableResource> resourceList;
	private Map<String, ObserverState> observers;
	private static Scanner scanner;
	private ServerState state;

	public ProxyObserver() {
		proxyObserver = new CoapServer();
		observers = new HashMap<String, ObserverState>();
		resourceList = new ArrayList<ObservableResource>();
		System.out.println("Starting listening...");
		proxyObserver.start();
	}

	public void init() {
//		Collection<Resource> resources = readResourcesFile();
//		if (resources == null)
//			return;
//		System.out.println(resources.toString());
//		addAllResources(resources);
	}

	public void setState(ServerState state) {
		this.state = state;
		resourceList.forEach(r -> r.setServerState(state));
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

	public void addResource(ObservableResource resource) {
		proxyObserver.add(resource);
		resourceList.add(resource);
		System.out.println("Resource \"" + resource.getName() + "\" added to the resource list\n");

//		resourceList = new DiscoveryResource(proxyObserver.getRoot());
//		proxyObserver.add(resourceList);
//		updateResourcesFile(resource);
	}

	public void addAllResources(Collection<Resource> resources) {
		for (Resource resource : resources) {
			proxyObserver.add(resource);
		}
	}

	public void updateResourcesFile(ObservableResource resource) {
		String pathname = "Resources/" + resource.getName() + ".bin";
		try (FileOutputStream fout = new FileOutputStream(new File(pathname));
				ObjectOutputStream bout = new ObjectOutputStream(fout)) {
			bout.writeObject(resource);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public Collection<Resource> readResourcesFile() {
		Collection<Resource> resources = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(Paths.get("Resources"))) {

			java.util.List<String> result = paths.map(x -> x.toString()).filter(f -> f.endsWith(".xml"))
					.collect(Collectors.toList());
			System.out.println(result);

			result.forEach(fileName -> {
				try (FileInputStream fin = new FileInputStream(new File(fileName));
						ObjectInputStream bin = new ObjectInputStream(fin)) {
					resources.add((ObservableResource) bin.readObject());
				} catch (IOException | ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return resources;
	}

	public void printHelpMenu() {
		String commandList = "1) Start the server\n" + "2) Add a resource\n" + "3) Delete a resource\n"
				+ "4) Print Help Menu\n" + "5) Exit\n" + "6) Simulate a Resource Change \n"
				+ "7) Change Server State \n";
		System.out.println("List of commands:\n" + commandList);
	}

	public void changeStateCLI() {
		System.out.print("Change Server State: ( 1 - AVAILABLE, 2 - ONLY_CRITICAL, 3 - UNAVAILABLE )\n");
		int cmd = scanner.nextInt();
		switch (cmd) {
		case 1:
			setState(ServerState.AVAILABLE);
			break;
		case 2:
			setState(ServerState.ONLY_CRITICAL);
			break;
		case 3:
			setState(ServerState.UNVAVAILABLE);
			break;
		default:
			System.out.print("Invalid State\n");
			break;
		}
	}

	public void addResourceCLI() {
//		System.out.print("Add Resourse\n");
//		System.out.print("Resource Name: ");
//		String resourceName = scanner.next();
		ObservableResource or = new ObservableResource();
		or.setName("prova");
		or.setServer(this);
		addResource(or);
	}

	public void deleteResourceCLI() {
		System.out.println("work in progress...");
	}

	public void triggerChange() {
		System.out.println(resourceList.get(0).getObserverCount());
		resourceList.get(0).changed();
	}

	public void clearObservation() {
		resourceList.get(0).clearObserveRelations();
	}

	public static void main(String[] args) {
		ProxyObserver server = new ProxyObserver();
		scanner = new Scanner(System.in);

		System.out.println("Welcome to the ProxyObserver Command Line Interface");
		server.printHelpMenu();
		server.addResourceCLI();
		while (true) {
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
				server.printHelpMenu();
				break;
			case 5:
				System.out.println("Exiting... Good bye!");
				System.exit(0);
				break;
			case 6:
				server.triggerChange();
				break;
			case 7:
				server.changeStateCLI();
				break;
			default:
				continue;
			}
		}
	}
}
