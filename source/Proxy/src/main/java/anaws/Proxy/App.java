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
    }
}
