package anaws.Proxy.ProxySubject;

import java.util.*;
import org.eclipse.californium.core.*;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ProxyObserver;



public class ProxySubject{

	final int NUMBER_SENSORS = 1;	
	
	ProxyObserver proxyObserver;
	Registrator registrator;
	CacheTable cache;
	CoapClient coapClient;
	SensorList sensors;
	
	public ProxySubject(ProxyObserver observerModule) {
		this.proxyObserver = observerModule;
		this.registrator = new Registrator();
		this.cache = new CacheTable();
		this.coapClient = new CoapClient();
		this.sensors = new SensorList();
		new Updater(this.cache, this.registrator).start();
		for (int i = 2; i <= this.NUMBER_SENSORS+1;i++)
			prepareResources("fd00::c30c:0:0:"+i,5683);
		this.sensors.printSensors();
		// Registration for the battery information
		for (SensorNode sensor : sensors.getAllSensors()) {
			this.newRegistration(sensor, "battery", false);
		}
	}
	public boolean newRegistration(SensorNode sensor,String type,boolean critic){
		Log.info("ProxySubject", "Request for new registration");
		Registration r = new Registration(this.cache,sensor,type,critic,coapClient);
		int result = registrator.newRegistration(r);
		if ( result == 1 ) {
			proxyObserver.startNotificationListener(r);
		} else if(result == 2) {
			cache.updateRegistrations(r);
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
			if(x.getURI().contains("/sensors/")) {
				String resourceName = x.getURI().substring(9);
				s.addResource(x.getURI().substring(9));
				if(!x.getURI().contains("battery")) {
					proxyObserver.addResource(s, resourceName, first);
					first = false;
				}
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
