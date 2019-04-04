package anaws.Proxy.ProxySubject;

import java.net.InetAddress;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.coap.CoAP.Code;

import anaws.Proxy.Log;
import anaws.Proxy.ProxyObserver.ProxyObserver;


public class Registration{

	CacheTable cache;
	SensorNode sensor;
	String type;
	boolean critic;
	CoapClient coapClient;
	CoapObserveRelation coapRelation;
	ProxyObserver observer;
	
	public Registration(CacheTable _cache,SensorNode _sensor,String _type,boolean _critic,ProxyObserver observer,CoapClient coap){
		this.cache = _cache; 
		this.sensor = _sensor;
		this.type = _type;
		critic = _critic;
		this.coapClient = new CoapClient();
		this.observer = observer;
	}
	public Registration(CacheTable _cache,SensorNode _sensor,String _type,boolean _critic,CoapClient coap){
		this.cache = _cache; 
		this.sensor = _sensor;
		this.type = _type;
		critic = _critic;
		this.coapClient = coap;
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
		 String URI = "coap://[" + address + "]:" + port + "/sensors/"+path;
		 observeRequest.setURI(URI);	
		 System.out.println("----------------------------------REGISTRAZIONE--------------------------");
		 System.out.println(observeRequest.toString());
		 coapRelation = coapClient.observe(observeRequest, new ResponseHandler(this.cache,this,this.observer));
		 if(coapRelation.isCanceled() == false) {
			 //System.out.println("Registrazione con il sensore avvenuta");
			 Log.info("Registration", "Registration Succeded");
			 return true;
		 }else {
			 Log.error("Registration", "Error during the registration");
			 return false;
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
