
package anaws.Proxy.ProxySubject;


import java.sql.Timestamp;
import java.util.Scanner;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

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
			System.out.println("Risorsa non raggiungibile");
			onError();
			return;
		}
		if (!response.getOptions().hasObserve() ) {
			System.out.println("Risorsa non presente");
			onError();
			return;
		}
		// Creating the sensorData
		double Value = Double.valueOf(response.getResponseText());
		long maxAge = response.getOptions().getMaxAge();
		boolean critic = (response.getOptions().getObserve() == CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY)?true:false;
		SensorData newData = new SensorData(this.registration,Value,maxAge,critic);
		cache.insertData(newData);
		this.observer.triggerChange(newData);
		
	}
	public void onError() {
		
	}

}