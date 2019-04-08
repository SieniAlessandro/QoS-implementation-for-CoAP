package anaws.Proxy;

import anaws.Proxy.ProxyObserver.ProxyObserver;
import anaws.Proxy.ProxySubject.*;

public class Proxy 
{
    public static void main( String[] args )
    {
    	ProxyObserver proxyObserver = new ProxyObserver(false);
    	
    	ProxySubject proxySubject = new ProxySubject(proxyObserver);
    	proxyObserver.addProxySubject(proxySubject);
    	    	
    }
}
