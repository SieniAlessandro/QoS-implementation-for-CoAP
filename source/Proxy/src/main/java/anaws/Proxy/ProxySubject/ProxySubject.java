package anaws.Proxy.ProxySubject;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;



public class ProxySubject{

	public static void main(String args[]) {
		CacheTable cache = new CacheTable();
		SensorNode s = new SensorNode("1",1000);
		SensorNode s2 = new SensorNode("2",1000);
		SensorNode s3 = new SensorNode("3",1000);
		Registrator r = new Registrator();
		Registration reg = new Registration(s,"temp",false);
		Registration reg2 = new Registration(s2,"temp",false);
		Registration reg3 = new Registration(s3,"temp",false);
		r.newRegistration(reg);
		r.newRegistration(reg2);
		r.newRegistration(reg3);
		new Generator(reg,cache).start();
		new Generator(reg2,cache).start();
		new Generator(reg3,cache).start();
		//r.newRegistration(r4);
		//r.newRegistration(r5);
		//cache.insertData(new SensorData(s,1.0,"Temperature",10,true));
		/*
		Updater u = new Updater(cache);
		u.start();
		try{
			Thread.sleep(1000);
			cache.insertData(new SensorData(s,1.0,"Temperature",10,false));
			Thread.sleep(1000);
				cache.insertData(new SensorData(s,2.0,"Temperature",20,false));
		}catch(Exception e){}
		*/
	}
}
