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
	public ProxySubject(ProxyObserver observerModule) {
		this.proxyObserver = observerModule;
		this.registrator = new Registrator();
		this.cache = new CacheTable();
		this.coapClient = new CoapClient();
		new Updater(this.cache, this.registrator).start();
	}
	public void newRegistration(SensorNode sensor,String type,boolean critic){
		Registration r = new Registration(this.cache,sensor,type,critic,coapClient);
		int result = registrator.newRegistration(r);
		if(result == 2)
			cache.updateRegistrations(r);
		else if(result == -1){
		}
	} 
	public SensorData getValue(String resource,String type){
		return cache.getData(resource, type);
	}
}
