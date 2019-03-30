package anaws.Proxy.ProxySubject;
import java.util.*;

public class Registrator{
	ArrayList<Registration> reg;
	public Registrator(){
		reg = new ArrayList<Registration>();
	}
	
	synchronized public int newRegistration(Registration _r){
		int registrationNeeded = this.RegistrationNeeded(_r);
		if( registrationNeeded == 1){
			System.out.println("Registrator: Nuova registrazione necessaria");
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
			System.out.println("Registrator: Aggiornamento registrazione");
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
			System.out.println("Registrator: Registrazione non necessaria");
			return 0;
		}

	}
	synchronized private int RegistrationNeeded(Registration _r){
		for (Registration r: reg){
			System.out.println(r.getSensorNode().toString().equals(r.getSensorNode().toString()));
			if(r.equals(_r))
				return 0;
			else if(r.getSensorNode().toString().equals(r.getSensorNode().toString())){
				if(r.getType() == _r.getType() && (_r.isCritic() == false && r.isCritic() == true)){
					return 2;
				}
				else
					return 0;
			}
		}
		return 1;
	}
	synchronized private Registration findAssociate(Registration _r) {
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
			System.out.println("Registrazione associata rimossa");
		}
		else {
			System.out.println("Registrazione non presente");
		}
		
		

	}
}