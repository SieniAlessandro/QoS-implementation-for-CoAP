
package anaws.Proxy.ProxySubject;


import java.sql.Timestamp;
import java.util.Scanner;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.ServerState;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ProxyObserver;

import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;

public class ResponseHandler implements CoapHandler {
	final private boolean DEBUG = true;
	private CacheTable cache;
	private Registration registration;
	private ProxyObserver observer;
	public ResponseHandler(CacheTable cache,Registration registration,ProxyObserver observer) {
		this.cache = cache;
		this.registration = registration;
		this.observer = observer;
	}
	public void onLoad(CoapResponse response) {
		
		if (DEBUG) 
			System.out.println("---------------------------------------");
		if ( response.getCode().equals(CoAP.ResponseCode.SERVICE_UNAVAILABLE) ) {
			Log.error("ResponseHandler", "resource unreachable");
			return;
		}
		if (!response.getOptions().hasObserve() ) {
			Log.error("ResponseHandler", "resource not available: " + response.advanced().toString());
			return;
		}
		// Creating the sensorData
		if(this.registration.getType().equals("battery")) {
			//Updating battery
			Log.info("ResponseHandler", "Updating battery Level"); 
			this.registration.getSensorNode().updateBattery(Double.valueOf(response.getResponseText()));
		}
		else {
			String Message = response.getResponseText();
			double Value;
			long maxAge = response.getOptions().getMaxAge();
			boolean critic;

			if(Message.contains("!")) {
				critic = true;
				Value = Double.valueOf(Message.substring(0, Message.indexOf("!")));
			}
			else {
				critic = false;
				Value = Double.valueOf(Message);
			}
			System.out.println("Ricevuto nuovo valore: " + Value);
			SensorData newData = new SensorData(this.registration,Value,maxAge,critic);
			cache.insertData(newData);
			
			synchronized(registration) {
				this.registration.notify();
			}
		}
		
	}
	
	public void onError() {
		
	}

}