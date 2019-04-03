package anaws.Proxy.ProxySubject;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.californium.core.*;

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
	public void newRegistration(SensorNode sensor,String type,boolean critic){
		System.out.println("ProxySubject: Richiesta nuova registrazione");
		Registration r = new Registration(this.cache,sensor,type,critic,coapClient);
		int result = registrator.newRegistration(r);
		if(result == 2) {
			cache.updateRegistrations(r);
		}
		else if(result == -1){
			this.proxyObserver.clearObservation(sensor, type);
		} else if ( result == 1 ) {
			cache.insertData(new SensorData(r, 0.0, 500, false));
			proxyObserver.startNotificationListener(r);
		}
	} 
	public SensorNode getSensorNode(String URI) {
		return this.sensors.getSensor(URI);
	}
	private void prepareResources(String address,int port) {
		SensorNode s = new SensorNode(address,port);
		coapClient.setURI("coap://"+s.getUri());
		HashSet<WebLink> a = new HashSet<WebLink>();
		a.addAll((Set<WebLink>)coapClient.discover());
		boolean first = true;
		for (WebLink x : a) {
			if(x.getURI().contains("/sensors/")) {
				String resourceName = x.getURI().substring(9);
				s.addResource(x.getURI().substring(9));
				proxyObserver.addResource(s, resourceName, first);
				first = false;
			}
		}
		//ArrayList<String> offeredResources = s.getResources();
		/*for (String se: offeredResources) {
			System.out.println("******");
			System.out.println(se);
		}*/
		this.sensors.addSensor(s);
	}
	public SensorData getValue(String resource,String type){
		return cache.getData(resource, type);
	}
}
