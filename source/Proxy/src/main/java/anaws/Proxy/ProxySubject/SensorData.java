package anaws.Proxy.ProxySubject;



public class SensorData{

	volatile double value;
	volatile long maxAge;
	volatile boolean critic;
	volatile Registration registration;
	volatile long observe;

	public SensorData(Registration registration, double value,long maxAge,long observe,boolean isCritic){
		this.registration = registration;
		this.value = value;
		this.maxAge = maxAge;
		this.critic = isCritic;
		this.observe = observe;
	}
	synchronized public void updateValue(double value,long maxAge,long observe,boolean isCritic){
		this.value = value;
		this.maxAge = maxAge;
		this.observe = observe;
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
	
	synchronized public long getObserve() {return this.observe;}

	synchronized public Registration getRegistration(){ return this.registration;}
	
	public void changeRegistration(Registration r) {
		this.registration = r;
	}
	public String toString() {
		return "Value: "+value+" | Coming from: "+registration.getSensorNode().getUri()+" | Type: "+registration.getType()
	 +" | with MaxAge: "+maxAge+" | with observer: "+ this.observe+" is critic: "+this.critic;
	}
}