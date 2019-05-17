package anaws.Proxy.ProxyObserver;

import java.util.HashMap;

import anaws.Proxy.Log;
import anaws.Proxy.ProxySubject.ProxySubject;
import anaws.Proxy.ProxySubject.Registration;
import anaws.Proxy.ProxySubject.SensorData;
import anaws.Proxy.ProxySubject.SensorNode;

import java.util.Map;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.Resource;

import org.eclipse.californium.core.server.ServerState;

public class ProxyObserver {

	private CoapServer proxyObserver;
	final private int MAX_RETRANSMISSION = 10;

	private volatile ProxySubject proxySubject;
	private Map<String, ObservableResource> resourceList;
	private Map<String, ObserverState> observers;

	public ProxyObserver() {
		NetworkConfig config = NetworkConfig.getStandard();
		config.setInt(NetworkConfig.Keys.MAX_RETRANSMIT, MAX_RETRANSMISSION);
		this.proxyObserver = new CoapServer(config, 5683);
		this.observers = new HashMap<String, ObserverState>();
		this.resourceList = new HashMap<String, ObservableResource>();

		Log.info("ProxyObserver", "Coap Server Started");
		proxyObserver.start();
	}

	public void addProxySubject(ProxySubject proxySubject) {
		this.proxySubject = proxySubject;
	}

	public void addObserver(String key, ObserverState state) {
		observers.put(key, state);
	}
	
	public void removeObserver(String key) {
		observers.remove(key);
	}

	public ObserverState getObserverState(String key) {
		return observers.get(key);
	}

	public boolean isObserverPresent(String key) {
		return observers.containsKey(key);
	}

	private String getUnbrachetAddress(String sensor) {
		String address = sensor.replace("[", "");
		return address = address.replace("]", "");
	}

	public ObservableResource getResource(SensorNode sensor, String resourceName) {
		String address = getUnbrachetAddress(sensor.getUri());
		String key = "/" + address + "/" + resourceName;
		ObservableResource resource = resourceList.get(key);
		if (resource == null)
			Log.error("ProxyObserver", "Resource not found");
		return resource;
	}

	public void updateResource(SensorNode sensor, String resourceName, SensorData data) {
		getResource(sensor, resourceName).setSensorData(data);
	}

	/****************************************
	 * INTERACTION WITH PROXYSUBJECT MODULE *
	 ****************************************/
	
	public void resourceChanged(SensorNode sensor, String resourceName) {
		SensorData data = requestValueCache(sensor, resourceName);
		boolean isCritical = data.getCritic();
		ObservableResource resource = getResource(sensor, resourceName);
		Log.info("ProxyObserver",
				"" + sensor.getUri() + "/" + resourceName + " changed, isCritical: " + data.getCritic() + ". Value: "
						+ data.getValue() + ". Current observers: " + resource.getObserverCount());

		updateResource(sensor, resourceName, data);
		if (!isCritical)
			resource.changed(new CriticalRelationFilter());
		else
			resource.changed();
	}

	public void addResource(SensorNode sensor, String resourceName, boolean first) {
		ObservableResource resource = new ObservableResource(resourceName, this, sensor.getUri());

		if (first) {
			// first resource of this sensor then create the sensorResource that has the
			// added resource as child
			CoapResource sensorResource = new ObservableResource(getUnbrachetAddress(sensor.getUri()), this,
					sensor.getUri());
			sensorResource.setVisible(false);
			sensorResource.add(resource);
			proxyObserver.add(sensorResource);
		} else {
			// sensor already present
			for (Resource r : proxyObserver.getRoot().getChildren()) {
				if (r.getName().equals(getUnbrachetAddress(sensor.getUri())))
					r.add(resource);
			}
		}

		resourceList.put(resource.getURI(), resource);

		Log.info("ProxyObserver", "Resource \"" + resource.getName() + "\" of sensor \"" + sensor.getUri()
				+ "\" added to the resource list\n");
	}

	public void deleteResource(SensorNode sensor, String resourceName) {

		Resource sensorResource = null;
		// find sensor resource
		for (Resource sr : proxyObserver.getRoot().getChildren()) {
			if (sr.getName().equals(getUnbrachetAddress(sensor.getUri()))) {
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
		}

		Log.info("ProxyObserver", "Resource \"" + resourceName + "\" of sensor \""
				+ getUnbrachetAddress(sensor.getUri()) + "\" removed from the resource list\n");
	}

	public void clearObservation(SensorNode sensor, String resourceName) {
		String key = "/" + getUnbrachetAddress(sensor.getUri()) + "/" + resourceName;

		resourceList.get(key).clearAndNotifyObserveRelations(CoAP.ResponseCode.SERVICE_UNAVAILABLE);
	}

	public void clearAllObservation() {
		resourceList.values().forEach(r -> r.clearAndNotifyObserveRelations(CoAP.ResponseCode.SERVICE_UNAVAILABLE));
	}

	public void clearObservationAfterStateChanged(String sensorAddress, String resourceName, ServerState state) {
		// Clear all the observaRelation of each resource of that sensor
		String key = "/" + getUnbrachetAddress(sensorAddress) + "/" + resourceName;
		ObservableResource o = resourceList.get(key);
		Log.info("ProxyObserver", "Clear relations after sensor state changed: " + key + " | " + state);
		if (state == ServerState.ONLY_CRITICAL) {
			o.clearAndNotifyNonCriticalObserveRelations(CoAP.ResponseCode.FORBIDDEN);
		} else if (state == ServerState.UNAVAILABLE) {
			o.clearAndNotifyObserveRelations(CoAP.ResponseCode.SERVICE_UNAVAILABLE);
		}
	}

	public SensorData requestValueCache(SensorNode sensor, String resourceName) {
		SensorData data = proxySubject.getValue(sensor.getUri(), resourceName);
		return data;
	}

	public boolean requestRegistration(SensorNode sensor, String resourceName, boolean critical) {
		return proxySubject.newRegistration(sensor, resourceName, critical);
	}

	public SensorNode requestSensorNode(String sensorAddress) {
		return proxySubject.getSensorNode(sensorAddress);
	}

	public void requestObserveCancel(Registration registration) {
		proxySubject.removeRegistration(registration);
	}
}
