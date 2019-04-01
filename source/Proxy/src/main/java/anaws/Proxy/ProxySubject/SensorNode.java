package anaws.Proxy.ProxySubject;

import java.util.ArrayList;

import org.eclipse.californium.core.server.ServerState;

public class SensorNode{

	String IPaddress;
	int Port;
	double battery;
	ServerState actualState;
	ArrayList<String> resources;
	
	public SensorNode(String address,int port){
		IPaddress = address;
		Port = port;
		battery = 100;
		actualState = ServerState.AVAILABLE;
		this.resources = new ArrayList<String>();
	}
	public SensorNode(String address,int port,ArrayList<String> resources) {
		this.IPaddress = address;
		this.Port = port;
		this.resources = new ArrayList<String>();
		this.resources.addAll(resources);
		this.battery = 100;
		actualState = ServerState.AVAILABLE;
	}
	public void addResource(String resource) {
		for(String s : this.resources) {
			if(s.equals(resource))
				return;
		}
		this.resources.add(resource);
	}
	public ArrayList<String> getResources() {return this.resources;}
	
	public ServerState updateBattery(double newBatteryValue) {
		System.out.println("Aggiornamento Valore Batteria");
		battery = newBatteryValue;
		if(battery <= 30)
			actualState = ServerState.ONLY_CRITICAL;
		else if(battery <= 0) 
			actualState = ServerState.UNVAVAILABLE;
		return actualState;

	}
	
	public String getAddress() {return this.IPaddress;}
	public int getPort() {return this.Port;}
	public ServerState getState(){return actualState;}
	public String getUri(){
		return "["+IPaddress+"]:"+Integer.toString(Port);
	}
	public String toString(){
		return IPaddress+":"+Integer.toString(Port)+" | "+this.resources.size()+" resources";
	}
	@Override
	public boolean equals(Object obj) 
    {
    	if(this == obj)
    		return true;
    	if(obj == null || obj.getClass()!= this.getClass()) 
            return false; 
        SensorNode s = (SensorNode) obj;
        return (this.toString().equals(s.toString()));
    } 
}