package anaws.Proxy.ProxySubject;

import java.util.*;

public class Updater extends Thread{
	CacheTable c;
	Registrator r;

	public Updater(CacheTable cache,Registrator reg){
		c = cache;
		r = reg;
	}
	@Override
	public void run(){
		while (true){
			try{
				Thread.sleep(1000);	
				ArrayList<Registration> toDelete = c.updateTime(1);
				for (Registration d : toDelete){
					r.removeRegistration(d);
				}
			}catch(InterruptedException ex){
				ex.printStackTrace();
			}
		}
	}

}