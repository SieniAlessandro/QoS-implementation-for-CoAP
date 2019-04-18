package anaws.Proxy.ProxyObserver;


import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

import anaws.Proxy.Log;
import anaws.Proxy.ProxySubject.SensorData;
import anaws.Proxy.ProxySubject.SensorNode;

import org.eclipse.californium.core.server.ServerState;

public class ObservableResource extends CoapResource {

	final private boolean DEBUG = false;
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
		super(name);
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
			Log.info("ObservableResource", "Cancel observe request from " + exchange.getSourcePort()
					+ " for the resource: " + exchange.advanced().getRequest().getURI());
			return;
		}
		if (sensor.getState().equals(ServerState.UNVAVAILABLE)) {
			System.out.println("Subject is unavailable");
			return;
		}
		// store observer information if the endpoint is not already present
		String observerID = exchange.getSourceAddress() + ":" + exchange.getSourcePort() + "/" + getName();
		int mid = exchange.advanced().getRequest().getMID();
		if (!server.isObserverPresent(observerID)) {
			server.addObserver(observerID, new ObserverState(mid, false));
			handleRegistration(observeField, observerID, exchange, sensor);
			return;
		}
		
		if (mid == server.getObserverState(observerID).getOriginalMID()) {
			// This is a notification because the exchange has the same MID of the original
			// request
			sendNotification(exchange, sensor, -1);
		} else {
			// The observer is already present but this is not a notification then it is a
			// request of reregistration
			handleRegistration(observeField, observerID, exchange, sensor);
		}
	}

	private void handleRegistration(int observeField, String observerID, CoapExchange exchange, SensorNode sensor) {
		// Registration phase
		Log.debug("ObservableResource", "Request: " + exchange.advanced().getRequest().toString());
		if (!server.getObserverState(observerID).isNegotiationState()) {
			if (getPriority(observeField) < 3 && sensor.getState().equals(ServerState.ONLY_CRITICAL) ) {
				// First part of the negotiation, where subject make its proposal
				Response response = new Response(CoAP.ResponseCode.NOT_ACCEPTABLE);
				response.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, PROPOSAL)));
				server.getObserverState(observerID).setNegotiationState(true);
				exchange.respond(response);
				Log.info("ObservableResource", "Negotiation Started: " + response.toString());

			} else {
				Log.info("ObservableResource", "Accepting the request from " + exchange.getSourcePort()
						+ " request without negotiation: " + exchange.getRequestOptions().toString());
				server.getObserverState(observerID).setOriginalMID(exchange.advanced().getRequest().getMID());

				boolean registrationOk = server.requestRegistration(sensor, getName(), getPriority(observeField) > 2 ? true : false);
				if (registrationOk) {
					// Request accepted without negotiation
					Log.info("ObservableResource", "Registration done proxy - subject done!");
					data = server.requestValueCache(sensor, getName());
					sendNotification(exchange, sensor, observeField);
				} else {
					Log.error("ObservableResource", "Registration Proxy-Subject failed ( No Negotiation )");
					exchange.respond(CoAP.ResponseCode.NOT_FOUND);
				}
			}
		} else {
			// This is the second part of a negotiation
			Log.info("ObservableResource", "Second Part Negotiation ");
			server.getObserverState(observerID).setNegotiationState(false);
			server.getObserverState(observerID).setOriginalMID(exchange.advanced().getRequest().getMID());

			boolean registrationOk = server.requestRegistration(sensor, getName(), getPriority(observeField) > 2 ? true : false);
			if (registrationOk) {
				Log.info("ObservableResource", "Negotiation ended ");
				data = server.requestValueCache(sensor, getName());
				sendNotification(exchange, sensor, -1);
			} else {
				Log.error("ObservableResource", "Registration Proxy-Subject failed after a negotiation");
				exchange.respond(CoAP.ResponseCode.NOT_FOUND);
			}
		}
	}

	private void sendNotification(CoapExchange exchange, SensorNode sensor, int observeField) {
		double value = data.getValue();
		Response response = new Response(CoAP.ResponseCode.CONTENT);
		response.setPayload(Double.toString(value));
		exchange.setMaxAge(data.getTime());

		if (observeField < 0) {
			exchange.respond(response, (int) data.getObserve());
//			exchange.respond(response);
		} else {
			// This is a registration response, respond with the same observe number ;
			exchange.respond(response, observeField);
		}
		
		Log.debug("ObservableResource", "Response: " + response.toString());
		Log.info("ObservableResource", "Notification sent to: " + exchange.getSourcePort() + " | notification: " + value
				+ " | isCritical: " + data.getCritic());
	}
}


// TODO rispondere alla registrazione con lo stesso campo observe ricevuto!
//		implementare nell'observer la possibilità di accettare o meno la proposta del server
//		implementare scrittura file CSV