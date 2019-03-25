public class SensorNode{

	String IPaddress;
	int Port;

	public SensorNode(String address,int port){
		IPaddress = address;
		Port = port;
	}
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