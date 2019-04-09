package anaws.Proxy.ProxySubject;

import java.util.ArrayList;
import java.util.concurrent.*;

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
	synchronized public boolean addSensor(String address,int port, ArrayList<String> resources){
		
		if (getSensor(address+":"+port) == null) {
			return false;
		}
		else {
			SensorNode sensor = new SensorNode(address,port,resources);
			this.sensors.add(sensor);
			return true;
		}
	}
	synchronized public SensorNode getSensor(String URI) {
		for(SensorNode sn : sensors) {
			if(sn.getUri().equals(URI)){
				return sn;
			}
		}
		return null;
	}
	synchronized public ArrayList<SensorNode> getAllSensors() { return this.sensors;}
	synchronized public void printSensors() {
		for(SensorNode s: sensors) {
			System.out.println(s);
		}
	}
}
