package anaws.Proxy.ProxySubject;
import java.util.*;

import anaws.Proxy.Log;

public class Registrator{
	
	ArrayList<Registration> reg;
	public Registrator(){
		reg = new ArrayList<Registration>();
	}
	
	synchronized public int newRegistration(Registration _r){
		int registrationNeeded = this.RegistrationNeeded(_r);
		if( registrationNeeded == 1){
			Log.info("Registrator", "New registration needed");
			boolean result = _r.register();
			//boolean result = true;
			if(result){
				this.reg.add(_r);
				return 1;
			}
			else
				return -1;
		}
		else if (registrationNeeded == 2) {
			Log.info("Registrator","Updating old registration");
			//System.out.println("Registrator: Aggiornamento registrazione");
			Registration r = findAssociate(_r);
			this.removeRegistration(r);
			boolean result = _r.register();
			//boolean result = true;
			if(result){
				this.reg.add(_r);
				return 2;
			}
			else
				return -1;
		}
		else{
			Log.info("Registrator", "New registration not needed");
			//System.out.println("Registrator: Registrazione non necessaria");
			return 0;
		}

	}
	synchronized private int RegistrationNeeded(Registration _r){
		for (Registration r: reg){
			if(r.equals(_r)) {
				Log.debug("Registrato", "Registration identical to another");
				return 0;
			}
			else if(r.getSensorNode().toString().equals(_r.getSensorNode().toString())) {
				if(r.getType() == _r.getType()) {
					if((_r.isCritic() == false && r.isCritic() == true)) {
						Log.debug("Registrator", "Registration equal except for the new critic");
						return 2;
					}
					else if (r.isCritic() == _r.isCritic() || _r.isCritic() == true && _r.isCritic() == false) {
						Log.debug("Registrato", "Registrator equal and the new critic is not sufficient to re register");
						return 0;
						
					}
				}
			}
		}
		return 1;
	}
	synchronized private Registration findAssociate(Registration _r) {
		Log.info("Registrator", "Finding the associated registration....");
		for (Registration r: reg){
			if(r.getSensorNode().toString().equals(r.getSensorNode().toString()) && 
			   r.getType() == _r.getType() && (_r.isCritic() == false && r.isCritic() == true))
			   	return r;
		}
		return null;
	}
	synchronized public void removeRegistration(Registration _r){
		if(reg.contains(_r)) {
			reg.remove(_r);
			_r.sendCancelation();
			synchronized(_r) {
				_r.notify();
			}
			Log.info("Registrator", "Registration requested removed");
		}
		else {
			Log.info("Registrator", "Registration requested not found");
		}
	}
}