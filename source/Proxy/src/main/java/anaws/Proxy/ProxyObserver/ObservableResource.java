package anaws.Proxy.ProxyObserver;

import java.sql.Timestamp;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.ConcurrentCoapResource;

import anaws.Proxy.ProxySubject.ProxySubject;
import anaws.Proxy.ProxySubject.SensorData;
import anaws.Proxy.ProxySubject.SensorNode;

import org.eclipse.californium.core.server.ServerState;

public class ObservableResource extends ConcurrentCoapResource {

	final private static int THREAD_POOL_SIZE = 2;

	final private boolean DEBUG = true;
	final private int PROPOSAL = CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY;
	
	private ProxyObserver server;
	private SensorData data;
	private String sensorAddress;

	public SensorData getSensorData() {
		return data;
	}

	public void setSensorData(SensorData data) {
		this.data = data;
	}

	public ObservableResource(String name, ProxyObserver server, String sensorAddress) {
		super(name, THREAD_POOL_SIZE);
		this.setObservable(true);
		this.setObserveType(Type.CON);
		this.setVisible(true);
		this.server = server;
		this.sensorAddress = sensorAddress;
	}

	public int getPriority(int priority) throws IllegalArgumentException {
		int dec;
		switch (priority) {

		case CoAP.QoSLevel.NON_CRITICAL_LOW_PRIORITY:
			dec = 1;
			break;
		case CoAP.QoSLevel.NON_CRITICAL_MEDIUM_PRIORITY:
			dec = 2;
			break;
		case CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY:
			dec = 3;
			break;
		case CoAP.QoSLevel.CRITICAL_HIGHEST_PRIORITY:
			dec = 4;
			break;
		default:
			throw new IllegalArgumentException();
		}
		return dec;
	}
	

	@Override
	public void handleGET(CoapExchange exchange) {
		System.out.println("---------------------------------------");
		int observeField = exchange.getRequestOptions().getObserve();
		
		SensorNode sensor = server.requestSensorNode(sensorAddress);

		if (observeField == 1) {
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Cancel observe request from " + exchange.getSourcePort()
			+ " for the resource: " + exchange.advanced().getRequest().getURI() );
			return;
		}
		if (sensor.getState().equals(ServerState.UNVAVAILABLE)) {
			System.out.println("Subject is unavailable");
			return;
		}
		
		int priority = getPriority(observeField);
		if (DEBUG)
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [DEBUG] handleGET request with priority: "
					+ priority);
		
		// store observer information if the endpoint is not already present
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort();
		int mid = exchange.advanced().getRequest().getMID();
		if (!server.isObserverPresent(observerID)) {
			if (DEBUG)
				System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [DEBUG] Observer " + observerID + " not present, added to the list ");
			server.addObserver(observerID, new ObserverState(mid, false));
			handleRegistration(priority, observerID, exchange, sensor);
			return;
		}
		
		if (DEBUG)
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [DEBUG] original MID: " + server.getObserverState(observerID).getOriginalMID() + " currentMID: " + mid);

		if (mid == server.getObserverState(observerID).getOriginalMID()) {
			// This is a notification because the exchange has the same MID of the original
			// request
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Resource value changed");
			sendNotification(exchange, sensor, false);
		} else {
			// The observer is already present but this is not a notification then it is a
			// request of reregistration
			handleRegistration(priority, observerID, exchange, sensor);
		}
	}

	private void handleRegistration(int priority, String observerID, CoapExchange exchange, SensorNode sensor) {
		// Registration phase
		if (!server.getObserverState(observerID).isNegotiationState()) {
			if (priority < 3 && sensor.getState().equals(ServerState.ONLY_CRITICAL)) {
				// First part of the negotiation, where subject make its proposal
				Response response = new Response(CoAP.ResponseCode.NOT_ACCEPTABLE);
				response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, PROPOSAL)));
				server.getObserverState(observerID).setNegotiationState(true);
				exchange.respond(response);
				if (DEBUG) {
					System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Negotiation Started: " + response.toString());
				}
			} else {
				System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Accepting the request from " + exchange.getSourcePort()
						+ " request without negotiation: " + exchange.getRequestOptions().toString());
				server.getObserverState(observerID).setOriginalMID(exchange.advanced().getRequest().getMID());
				if (DEBUG)
					System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [DEBUG] Current SensorData: " + this.data);
				server.requestRegistration(server.requestSensorNode(sensorAddress), getName(), priority > 2 ? true:false );
				// Request accepted without negotiation
				sendNotification(exchange, sensor, true);
			}
		} else {
			// This is the second part of a negotiation
			server.getObserverState(observerID).setNegotiationState(false);
			server.getObserverState(observerID).setOriginalMID(exchange.advanced().getRequest().getMID());
			
			server.requestRegistration(sensor, getName(), priority > 2 ? true:false );
			
			sendNotification(exchange, sensor, true);
			System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Negotiation ended ");
		}
	}

	private void sendNotification(CoapExchange exchange, SensorNode sensor, boolean refresh) {
		if ( refresh ) {
			data = server.requestValueCache(sensor, this.getName());
			if ( data == null ) {
				System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] ProxySubject avoided to send the refresh because the max is going to expire soon" );
				return;
			}
		}
		double value = data.getValue();
		exchange.setMaxAge(data.getTime());
		exchange.respond(Double.toString(value));
		System.out.println("[" + new Timestamp(System.currentTimeMillis()) + ")]  [INFO] Notification sent to : " + exchange.getSourcePort() + " notification: " + value);
	}
}
