import java.util.*;


public class CacheTable{
	ArrayList<SensorData> cache;
	public CacheTable(){
		cache = new ArrayList<SensorData>();
		System.out.println("Create tabella cache");
	}
	synchronized public ArrayList<Registration> updateTime(int time){
		ArrayList<Registration> toDelete = new ArrayList<Registration>();
		for(Iterator<SensorData> i = cache.iterator();i.hasNext();){
			SensorData d = i.next();
			System.out.println(d);
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
			System.out.println("Inserimento nuovo valore nella cache");
			cache.add(data);

			// RIMBALZA(SENSORDATA) AMEDEO
			return true;
		}
		//Otherwise the old value is updated 
		System.out.println("Aggiornamento vecchio valore");
		old.updateValue(data.getValue(),data.getTime());
		//RIMBALZA(SENSORDATA) AMEDEO
		return false;
	}
	synchronized private void removeData(SensorData data){
		//Removing the sensor data from the list
		cache.remove(data);
	}
	synchronized public SensorData findSensorData(Registration r){
		for(SensorData c : cache){
			if(c.getRegistration().equals(r))
				return c;
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
}