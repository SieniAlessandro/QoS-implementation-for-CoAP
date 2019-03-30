package anaws.Proxy.ProxySubject;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.californium.core.*;

import anaws.Proxy.ProxyObserver.ProxyObserver;



public class ProxySubject{

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
		//new Updater(this.cache, this.registrator).start();
		discoverResources();
		
	}
	public void newRegistration(SensorNode sensor,String type,boolean critic){
		System.out.println("ProxySubject: Richiesta nuova registrazione");
		Registration r = new Registration(this.cache,sensor,type,critic,coapClient,proxyObserver);
		int result = registrator.newRegistration(r);
		if(result == 2)
			cache.updateRegistrations(r);
		else if(result == -1){
			this.proxyObserver.clearObservation(sensor, type);
		}
	} 
	private void discoverResources() {
		HashSet<WebLink> a = (HashSet<WebLink>) coapClient.discover();
		for (WebLink x : a) {
			System.out.println(x.toString());
		}
	}
	public SensorData getValue(String resource,String type){
		return cache.getData(resource, type);
	}
}
