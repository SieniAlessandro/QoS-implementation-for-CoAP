package anaws.Proxy.ProxySubject;



public class SensorData{

	double value;
	long maxAge;
	boolean critic;
	Registration registration;

	public SensorData(Registration registration, double value,long maxAge,boolean isCritic){
		this.registration = registration;
		this.value = value;
		this.maxAge = maxAge;
		this.critic = isCritic;
	}
	synchronized public void updateValue(double value,long l){
		this.value = value;
		this.maxAge = l;
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
}