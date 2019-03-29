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
		registrator.newRegistration(r);
	} 
	public SensorData getValue(SensorNode sensor,String type){
		return cache.getData(sensor, type);
	}
}
