package anaws;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.observe.ObservingEndpoint;
import org.eclipse.californium.core.server.resources.DiscoveryResource;

public class ProxyObserver {
	
	private CoapServer proxyObserver;
	private DiscoveryResource resourceList; 
	private Map<String, Entry<CoapEndpoint, ObservingEndpoint>> observers;
	
	public ProxyObserver() {
				
	observers = new HashMap<String, Entry<CoapEndpoint, ObservingEndpoint>>();
//		resouceList: 
//			- read the list of resources from resources.conf 
//			- build a Resource root
//			- call the constructor DiscoveryResource(Resource root)
		
	}
	
	public void addObserver(String key, Endpoint e, ObservingEndpoint oe) {
		observers.put(key, new SimpleEntry((CoapEndpoint) e, oe));
	}
	
	public ObservingEndpoint getObservingEndpoint(String key) {
		return observers.get(key).getValue();
	}
	
	public CoapEndpoint getCoapEndpoint(String key) {
		return observers.get(key).getKey();
	}
	
	public boolean isEndpointPresent( String key ) {
		return observers.containsKey(key);
	}
	
	public static void main(String[] args) {

	}
}
