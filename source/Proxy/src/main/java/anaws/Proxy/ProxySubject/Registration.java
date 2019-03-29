package anaws.Proxy.ProxySubject;

import java.net.InetAddress;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.coap.CoAP.Code;

import anaws.Proxy.ProxyObserver.ProxyObserver;


public class Registration{

	CacheTable cache;
	SensorNode sensor;
	String type;
	boolean critic;
	CoapClient coapClient;
	CoapObserveRelation coapRelation;
	ProxyObserver observer;
	
	public Registration(CacheTable _cache,SensorNode _sensor,String _type,boolean _critic,CoapClient coapClient,ProxyObserver observer){
		this.cache = _cache; 
		this.sensor = _sensor;
		this.type = _type;
		critic = _critic;
		this.coapClient = coapClient;
		this.observer = observer;
	}
	public Registration(CacheTable _cache,SensorNode _sensor,String _type,boolean _critic,CoapClient coapClient){
		this.cache = _cache; 
		this.sensor = _sensor;
		this.type = _type;
		critic = _critic;
		this.coapClient = coapClient;
	}
	public boolean register() {
		return this.resourceRegistration(this.sensor.getAddress(),this.sensor.getPort(),(this.critic == true)?CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY:CoAP.QoSLevel.NON_CRITICAL_LOW_PRIORITY, this.type);
	}	
	private boolean resourceRegistration(String address,int port, int priority, String path) {
		Request observeRequest = new Request(Code.GET);
		try {
			// Set the priority level using the first 2 bits of the observe option value
			observeRequest.setObserve();
			observeRequest.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, priority)));
			} catch (IllegalArgumentException ex) {
			System.out.println("Invalid Priority Level");
		}
		 String URI = "coap://[" + address + "]:" + port + "/"+path;
		 observeRequest.setURI(URI);	
		 coapRelation = coapClient.observeAndWait(observeRequest,new ResponseHandler(this.cache,this,this.observer));
		 if(coapRelation.isCanceled() == false) {
			 System.out.println("Registrazione con il sensore avvenuta");
			 return false;
		 }else {
			 System.out.println("Errore nella registrazione");
			 return true;
		 }
		 
	}
	
	public void sendCancelation() {
		coapRelation.proactiveCancel();
	}
	
	public boolean isCanceled() {
		return coapRelation.isCanceled();
	}
	public SensorNode getSensorNode() { return sensor; }
	public String getType() { return type; }
	public boolean isCritic() { return critic; }


	@Override
	public boolean equals(Object obj)
  	{
  		if(this == obj)
    		return true;
    	if(obj == null || obj.getClass()!= this.getClass())
      	return false;
    	Registration s = (Registration) obj;
    	return (this.sensor.equals(s.sensor) && this.type.equals(s.getType()) && this.critic == s.isCritic());
  	}	

}
