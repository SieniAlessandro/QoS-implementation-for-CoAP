package anaws.Proxy;

import anaws.Proxy.ProxyObserver.ProxyObserver;
import anaws.Proxy.ProxySubject.*;
public class App 
{
    public static void main( String[] args )
    {
//    	ProxySubject.main(null);
    	ProxyObserver po = new ProxyObserver(false, false);
    	ProxySubject ps = new ProxySubject(po);	
    	ps.newRegistration(ps.getSensorNode("[fd00::c30c:0:0:2]:5683"), "temperature", true);
    	//ps.newRegistration(ps.getSensorNode("[fd00::c30c:0:0:3]:5683"), "temperature", true);
    }
}
