package anaws.Proxy.ProxySubject;

import java.util.ArrayList;

public class SensorList {
	ArrayList<SensorNode> sensors;
	public SensorList(){
		sensors = new ArrayList<SensorNode>();
	}
	synchronized public boolean addSensor(SensorNode newSensor){
		if(sensors.contains(newSensor))
			return false;
		else {
			sensors.add(newSensor);
			return true;
		}
	}
	synchronized public SensorNode getSensor(String URI) {
		for(SensorNode sn : sensors) {
			if(sn.toString().equals(URI)){
				return sn;
			}
		}
		return null;
	}
}
