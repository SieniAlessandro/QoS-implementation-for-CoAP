
package anaws.Proxy.ProxySubject;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ObservableResource;
import anaws.Proxy.ProxyObserver.ProxyObserver;

import org.eclipse.californium.core.coap.CoAP;


public class ResponseHandler implements CoapHandler {
	private CacheTable cache;
	private Registration registration;
	private ProxyObserver proxyObserver;
	public ResponseHandler(CacheTable cache,Registration registration,ProxyObserver proxyObserver) {
		this.cache = cache;
		this.registration = registration;
		this.proxyObserver = proxyObserver;
	}
	public void onLoad(CoapResponse response) {
		
		System.out.println("---------------------------------------");
		if ( response.getCode().equals(CoAP.ResponseCode.SERVICE_UNAVAILABLE) ) {
			Log.error("ResponseHandler", "resource unreachable");
			return;
		}
		if (!response.getOptions().hasObserve() && !response.advanced().getType().equals(CoAP.Type.ACK)) {
			Log.error("ResponseHandler", "resource not available: " + response.advanced().toString());
			return;
		}
		// Creating the sensorData
		if(this.registration.getType().equals("battery")) {
			//Updating battery
			Log.info("ResponseHandler", "Update battery level, new value: " + response.getResponseText());
			this.registration.getSensorNode().updateBattery(Double.valueOf(response.getResponseText()), proxyObserver);
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
			Log.info("ResponseHandler", "Ricevuto nuovo valore: " + Value );
			SensorData newData = new SensorData(this.registration,Value,maxAge,response.getOptions().getObserve(),critic);
			//Log.info("Repsponse Handler", "Inserted data:" +newData.toString());
			Log.LogOnFile("LogProxy.csv", newData.getExportLog());
			cache.insertData(newData);
			if(this.registration.isFirstValue() == true) {
				//In this case the only thing to do is to set firstValue at false
				this.registration.firstValueReceived();				
			}
			else {
				//Otherwise we must notify all the observers that a new value has arrived
				SensorNode sensor = registration.getSensorNode();
				String resourceName = registration.getType();
				ObservableResource resource = proxyObserver.getResource(sensor, resourceName);
				if (resource.getObserverCount() == 0) {
					Log.info("ResponseHandler", "No Observe Relations on this resource");
					proxyObserver.requestObserveCancel(registration);
				}
				proxyObserver.resourceChanged(sensor, resourceName);
			}
		}
	}
	
	public void onError() {
		
	}

}