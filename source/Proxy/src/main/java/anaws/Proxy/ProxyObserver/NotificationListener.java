package anaws.Proxy.ProxyObserver;

import anaws.Proxy.ProxySubject.Registration;
import anaws.Proxy.ProxySubject.SensorData;
import anaws.Proxy.ProxySubject.SensorNode;

public class NotificationListener extends Thread {

	private ProxyObserver proxyObserver;
	private Registration registration;

	public NotificationListener(ProxyObserver proxyObserver, Registration registration) {
		this.proxyObserver = proxyObserver;
		this.registration = registration;
	}

	public void run() {
		while (true) {
			try {
				synchronized (registration) {
					System.out.println("Notification Listener waiting ");
					registration.wait();
					System.out.println("Notification Listener wakes up ");
				}
				SensorNode sensor = registration.getSensorNode();
				String resourceName = registration.getType();
				
				
				ObservableResource resource = proxyObserver.getResource(sensor, resourceName);
				if (resource.getObserverCount() == 0) {
					System.out.println("No Observe Relations on this resource");
					continue;
				}
				System.out.println("Notification Listener notifies " + resource.getObserverCount() + " observers");
				proxyObserver.resourceChanged(sensor, resourceName);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
