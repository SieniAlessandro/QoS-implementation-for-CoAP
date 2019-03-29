package anaws.Proxy;

import org.eclipse.californium.core.CoapServer;

import anaws.Proxy.ProxyObserver.ProxyObserver;
import anaws.Proxy.ProxySubject.*;
public class Proxy 
{
    public static void main( String[] args )
    {
    	ProxyObserver proxyObserver = new ProxyObserver(false, true);
    	
    	ProxySubject proxySubject = new ProxySubject(proxyObserver);
    	proxyObserver.addProxySubject(proxySubject);
    	
    	// da sostituire quando sara' implementata la discorey tra proxy e sensori.
    	proxyObserver.addResourceCLI();
    	
//    	Avviare la discovert tra proxy e sensori 
//    	per ogni risorsa trovata 
//    		aggiungere un record alla cache ( anche senza valore )
//    		aggiungerla al proxyObserver
//    

    }
}
