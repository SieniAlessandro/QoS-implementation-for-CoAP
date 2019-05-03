package anaws.Proxy;

import anaws.Proxy.ProxyObserver.ProxyObserver;
import anaws.Proxy.ProxySubject.*;

public class Proxy 
{
    public static void main( String[] args )
    {
    	ProxyObserver proxyObserver = new ProxyObserver();
    	
    	ProxySubject proxySubject = new ProxySubject(proxyObserver, Integer.parseInt(args[0]));
    	proxyObserver.addProxySubject(proxySubject);
    	    	
    }
}
