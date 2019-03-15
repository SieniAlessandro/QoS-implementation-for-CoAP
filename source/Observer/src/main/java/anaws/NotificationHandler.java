package anaws;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.observe.NotificationListener;

public class NotificationHandler implements NotificationListener {

	public void onNotification(Request request, Response response) {
		System.out.println("[Notification]> " + response.getPayload().toString());
	}
	
}