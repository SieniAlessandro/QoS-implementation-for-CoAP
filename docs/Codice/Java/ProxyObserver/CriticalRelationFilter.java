package anaws.Proxy.ProxyObserver;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.observe.ObserveRelationFilter;

public class CriticalRelationFilter implements ObserveRelationFilter{

	public boolean accept(ObserveRelation relation) {
		return relation.getQoS() == CoAP.QoSLevel.NON_CRITICAL_LOW_PRIORITY || relation.getQoS() == CoAP.QoSLevel.NON_CRITICAL_MEDIUM_PRIORITY;
	}

}
