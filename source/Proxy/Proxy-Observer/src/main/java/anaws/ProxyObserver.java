package anaws;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.observe.ObservingEndpoint;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.DiscoveryResource;
import org.eclipse.californium.core.server.resources.Resource;

public class ProxyObserver {

	private CoapServer proxyObserver;
	private DiscoveryResource resourceList;
	private Map<String, Entry<CoapEndpoint, ObservingEndpoint>> observers;

	public ProxyObserver() {

		proxyObserver = new CoapServer();
		observers = new HashMap<String, Entry<CoapEndpoint, ObservingEndpoint>>();
//		resouceList: 
//			- ask to ProxySubject the list of resources from resources.conf 
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

	public boolean isEndpointPresent(String key) {
		return observers.containsKey(key);
	}

	public void addResource(CoapResource resource) {
		proxyObserver.add(resource);
		resourceList = new DiscoveryResource( proxyObserver.getRoot() );
		proxyObserver.add(resourceList);
	}
	
	public void start() {
		proxyObserver.start();
	}

	public static void main(String[] args) {
		ProxyObserver server = new ProxyObserver();
		server.addResource(new CoapResource("ciao") {
			public void handleGET(CoapExchange exchange) {
				exchange.respond(ResponseCode.CONTENT, "hello world");
			}
		});
		server.start();
		

	}
}
