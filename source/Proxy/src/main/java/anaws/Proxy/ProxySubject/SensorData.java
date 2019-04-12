package anaws.Proxy.ProxySubject;



public class SensorData{

	volatile double value;
	volatile long maxAge;
	volatile boolean critic;
	volatile Registration registration;

	public SensorData(Registration registration, double value,long maxAge,boolean isCritic){
		this.registration = registration;
		this.value = value;
		this.maxAge = maxAge;
		this.critic = isCritic;
	}
	synchronized public void updateValue(double value,long l,boolean isCritic){
		this.value = value;
		this.maxAge = l;
		this.critic = isCritic;
	}
	synchronized public boolean updateTime(long time){
		this.maxAge = this.maxAge - time;
		if(this.maxAge <= 0)
			return false;
		return true;
	}
	
	synchronized public double getValue(){ return this.value; }

	synchronized public long getTime(){ return this.maxAge; }

	synchronized public boolean getCritic(){ return this.critic; }

	synchronized public Registration getRegistration(){ return this.registration;}
	
	public void changeRegistration(Registration r) {
		this.registration = r;
	}
	public String toString() {
		return "Value: "+value+" | Coming from: "+registration.getSensorNode().getUri()+" | Type: "+registration.getType()
	 +" | with MaxAge: "+maxAge+" is critic: "+this.critic;
	}
}