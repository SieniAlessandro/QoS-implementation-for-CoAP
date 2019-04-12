package anaws.Proxy.ProxyObserver;

import anaws.Proxy.Log;
import anaws.Proxy.ProxySubject.Registration;
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
		Log.info("NotificationListener", "Notification Listener of resource " + sensor.getUri() + "/" + resourceName + " started" );
		while (!registration.isCanceled()) {
			try {
				synchronized (registration) {
					registration.wait();
				}
				if ( registration.isCanceled() ) 
					return;
				ObservableResource resource = proxyObserver.getResource(sensor, resourceName);
				if (resource.getObserverCount() == 0) {
					Log.info("NotificationListener", "No Observe Relations on this resource");
					proxyObserver.requestObserveCancel(registration);
					break;
				}
				proxyObserver.resourceChanged(sensor, resourceName);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
