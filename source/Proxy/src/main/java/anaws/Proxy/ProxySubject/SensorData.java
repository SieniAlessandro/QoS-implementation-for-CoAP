package anaws.Proxy.ProxySubject;



public class SensorData{

	double Value;
	//SensorNode Sensor;
	int Maxage;
	//String Type;
	boolean Critic;
	Registration Reg;

	/*public SensorData(SensorNode sensor,double value,String type,int maxage,boolean critic){
		Maxage = maxage;
		Value = value;
		Sensor = sensor;
		Type = type;
		Critic = critic;
	}*/
	public SensorData(Registration r, double v,int m,boolean c){
		Reg = r;
		Value = v;
		Maxage = m;
		Critic = c;
	}
	synchronized public void updateValue(double value,int maxage){
		Value = value;
		Maxage = maxage;
	}
	synchronized public boolean updateTime(int time){
		Maxage = Maxage - time;
		if(Maxage <= 0)
			return false;
		return true;
	}

	synchronized public double getValue(){ return Value; }

	synchronized public int getTime(){ return Maxage; }

	synchronized public boolean getCritic(){ return Critic; }

	synchronized Registration getRegistration(){ return Reg;}
}