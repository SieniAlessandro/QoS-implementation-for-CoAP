package anaws.Proxy.ProxySubject;


public class Registration{

	SensorNode Sensor;
	String Type;
	boolean Critic;

	public Registration(SensorNode s,String type,boolean critic){
		Sensor = s;
		Type = type;
		Critic = critic;
	}
	public Registration(String address,int port,String type,boolean critic){
		Sensor = new SensorNode(address,port);
		Type = type;
		Critic = critic;
	}
	
	public SensorNode getSensorNode() { return Sensor; }
	public String getType() { return Type; }
	public boolean isCritic() { return Critic; }


	@Override
	public boolean equals(Object obj)
  	{
  		if(this == obj)
    		return true;
    	if(obj == null || obj.getClass()!= this.getClass())
      	return false;
    	Registration s = (Registration) obj;
    	return (this.Sensor.equals(s.Sensor) && this.Type.equals(s.getType()) && this.Critic == s.isCritic());
  	}	

}
