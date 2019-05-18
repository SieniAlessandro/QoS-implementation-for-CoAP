package anaws.Proxy.ProxySubject;

import java.util.*;

import anaws.Proxy.Log;

public class CacheTable{
	private static final long THRESHOLD = 0;
	ArrayList<SensorData> cache;
	public CacheTable(){
		cache = new ArrayList<SensorData>();
		Log.info("CacheTable", "Cache Table created");
	}
	synchronized public ArrayList<Registration> updateTime(int time){
		ArrayList<Registration> toDelete = new ArrayList<Registration>();
		for(Iterator<SensorData> i = cache.iterator();i.hasNext();){
			SensorData d = i.next();
//			System.out.println(d.toString());
			if(d.updateTime(time) == false){
				i.remove();
				if(countRegistration(d.getRegistration()) == 1){
					toDelete.add(d.getRegistration());
				}
			}
		}
		return toDelete;
	}
	synchronized public boolean insertData(SensorData data){
		SensorData old = findSensorData(data.getRegistration());
		// Checking if there is an old data with the same type and coming from the same sensor
		if(old == null){
			//In this case there isn't any value and so the new value is appended
			cache.add(data);
			notifyAll();
			return true;
		}
		//Otherwise the old value is updated 
		old.updateValue(data.getValue(),data.getTime(),data.getObserve(),data.getCritic());
		return false;
	}
	synchronized private void removeData(SensorData data){
		//Removing the sensor data from the list
		cache.remove(data);
	}
	synchronized public SensorData findSensorData(Registration r){
		for(SensorData c : cache){
			if(c.getRegistration().isAssociated(r))
				return c;
		}
		return null;
	}
	synchronized public SensorData getData(String resource,String type) {
		SensorData sd;
		while((sd = searchData(resource,type)) == null) {
			try {
				Log.debug("CacheTable", "Waiting for data not null in cache: " +  resource + " " + type );
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return sd;
	}
	synchronized public SensorData searchData(String resource,String type) {
		for (SensorData sd : cache) {
			if(sd.getRegistration().getSensorNode().getUri().equals(resource) && sd.getRegistration().getType().equals(type)){
//				if(sd.getTime() <= this.THRESHOLD)
//					return null;
//				else
//					return sd;
				return sd;
			}
		}
		return null;		
	}
	synchronized public int countRegistration(Registration r){
		int ret = 0;
		for(SensorData d : cache){
			if(d.getRegistration().equals(r))
				ret = ret +1;
		}
		return ret;

	}
	synchronized public void updateRegistrations(Registration r) {
		for (SensorData d : cache) {
			if(d.getRegistration().equals(r)) {
				d.changeRegistration(r);
			}
		}
	}
}