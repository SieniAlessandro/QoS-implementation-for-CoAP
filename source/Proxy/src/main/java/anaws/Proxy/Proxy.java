package anaws.Proxy;

import org.eclipse.californium.core.CoapServer;

import anaws.Proxy.ProxyObserver.ProxyObserver;
import anaws.Proxy.ProxySubject.*;
public class Proxy 
{
    public static void main( String[] args )
    {
    	ProxyObserver proxyObserver = new ProxyObserver(false, false);
    	
    	ProxySubject proxySubject = new ProxySubject(proxyObserver);
    	proxyObserver.addProxySubject(proxySubject);
    	    	
    }
}
