package anaws;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.observe.ObserveRelationFilter;

public class CriticalRelationFilter implements ObserveRelationFilter{

	@Override
	public boolean accept(ObserveRelation relation) {
		return relation.getQoS() == CoAP.QoSLevel.CRITICAL_HIGH_PRIORITY || relation.getQoS() == CoAP.QoSLevel.CRITICAL_HIGHEST_PRIORITY;
	}

}
