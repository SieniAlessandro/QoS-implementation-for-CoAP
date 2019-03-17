package anaws;

import java.util.HashMap;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;

public class NegotiationHandler implements CoapHandler {

	private int priority;
	private HashMap<String, CoapObserveRelation> relations;
	private String resourceName; 
	
	public NegotiationHandler( HashMap<String, CoapObserveRelation> relations, int priority, String resourceName ) {
		this.relations = relations;
		this.priority = priority;
		this.resourceName = resourceName;
	}
	
	public void onLoad(CoapResponse response) {
		if (!response.getOptions().hasObserve()) {
			System.out.println("Subject rejected the observe request to the resource ");
			return;
		}
		
		if (response.getOptions().getObserve() == priority) {
			System.out.println("Observe Relation accepted");
			System.out.println(response.getResponseText());
			return;
		} else {		
			Request observeRequest = new Request(Code.GET);
			observeRequest.setOptions(new OptionSet().addOption(new Option(OptionNumberRegistry.OBSERVE, response.getOptions().getObserve())));
		}
		
	}

	public void onError() {
		// TODO Auto-generated method stub
		
	}
	
}