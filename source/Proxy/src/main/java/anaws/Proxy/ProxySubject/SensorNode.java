package anaws.Proxy.ProxySubject;

import org.eclipse.californium.core.server.ServerState;

public class SensorNode{

	String IPaddress;
	int Port;
	double battery;
	ServerState actualState;
	public SensorNode(String address,int port){
		IPaddress = address;
		Port = port;
		battery = 100;
		actualState = ServerState.AVAILABLE;
	}
	public ServerState updateBattery(double offset) {
		battery = battery - offset;
		if(battery <= 30)
			actualState = ServerState.ONLY_CRITICAL;
		else if(battery <= 0) 
			actualState = ServerState.UNVAVAILABLE;
		return actualState;

	}
	public ServerState getState(){return actualState;}
	
	public String toString(){
		return IPaddress+":"+Integer.toString(Port);
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