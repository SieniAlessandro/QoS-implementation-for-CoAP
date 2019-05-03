package anaws.Proxy.ProxySubject;

import java.util.*;
import org.eclipse.californium.core.*;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ProxyObserver;



public class ProxySubject{

	int NUMBER_SENSORS;	
	
	ProxyObserver proxyObserver;
	Registrator registrator;
	volatile CacheTable cache;
	CoapClient coapClient;
	SensorList sensors;
	
	public ProxySubject(ProxyObserver observerModule, int n) {
		this.NUMBER_SENSORS = n;
		this.proxyObserver = observerModule;
		this.registrator = new Registrator();
		this.cache = new CacheTable();
		this.coapClient = new CoapClient();
		this.sensors = new SensorList();
		new Updater(this.cache, this.registrator).start();
		for (int i = 1; i <= this.NUMBER_SENSORS;i++)
			prepareResources("fd00::c30c:0:0:"+i,5683);
		this.sensors.printSensors();
		// Registration for the battery information
		for (SensorNode sensor : sensors.getAllSensors()) {
			this.newRegistration(sensor, "battery", false);
		}
	}
	public boolean newRegistration(SensorNode sensor,String type,boolean critic){
		Log.info("ProxySubject", "Request for new registration");
		Registration r = new Registration(this.cache,sensor,type,critic, proxyObserver, coapClient);
		int result = registrator.newRegistration(r);
		if ( result == 1 ) {
			Log.info("ProxySubject", "New registration done");
		} else if(result == 2) {
			cache.updateRegistrations(r);
			Log.info("ProxySubject", "Registration updated");
		}
		else if(result == -1){
			this.proxyObserver.clearObservation(sensor, type);
			return false;
		}
		return true;
	} 
	public SensorNode getSensorNode(String URI) {
		return this.sensors.getSensor(URI);
	}
	private void prepareResources(String address,int port) {
		SensorNode s = new SensorNode(address,port);
		coapClient.setURI("coap://"+s.getUri());
		HashSet<WebLink> a = new HashSet<WebLink>();
		Log.info("ProxySubject", "Discovering for resources");
		a.addAll((Set<WebLink>)coapClient.discover());
		boolean first = true;
		for (WebLink x : a) {
			String uri = x.getURI();
			String resourceName = uri.substring(9);
			if(uri.contains("/sensors/") && !uri.contains("battery") ) {
				s.addResource(resourceName);
				proxyObserver.addResource(s, resourceName, first);
				first = false;
			}
		}
		this.sensors.addSensor(s);
	}
	public SensorData getValue(String resource,String type){
		return cache.getData(resource, type);
	}
	
	public void removeRegistration(Registration _r) {
		this.registrator.removeRegistration(_r);
	}
}
