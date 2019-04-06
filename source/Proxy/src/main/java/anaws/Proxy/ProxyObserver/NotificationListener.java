package anaws.Proxy.ProxyObserver;

import java.sql.Timestamp;

import anaws.Proxy.Log;
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
	@Override
	public void run() {
		SensorNode sensor = registration.getSensorNode();
		String resourceName = registration.getType();
		Log.info("ProxyObserver", "Notification Listener of resource " + sensor.getUri() + "/" + resourceName + " started" );
		while (true) {
			try {
				synchronized (registration) {
					registration.wait();
				}

				ObservableResource resource = proxyObserver.getResource(sensor, resourceName);
				if (resource.getObserverCount() == 0) {
					Log.info("ProxyObserver", "No Observe Relations on this resource");
					continue;
				}
				proxyObserver.resourceChanged(sensor, resourceName);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
