
package anaws.Proxy.ProxySubject;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ProxyObserver;

import org.eclipse.californium.core.coap.CoAP;


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
			if(this.registration.isFirstValue() == true) {
				//In this case the only thing to do is to set firstValue at false
				this.registration.firstValueReceived();				
			}
			else {
				//Otherwise we must notify all the observers that a new value has arrived
				synchronized(registration) {
					this.registration.notify();
				}
			}
		}
		
	}
	
	public void onError() {
		
	}

}