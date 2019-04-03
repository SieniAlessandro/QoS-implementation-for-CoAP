package anaws;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TestObserver {

	final static private int POOL_SIZE = 10;
	final static private String proxyIPv6 = "::1";
	final static private int proxyPort = 5683;

	public static void main(String[] args) {
//		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
		for (int i = 0; i < POOL_SIZE; i++) {
//			executor.execute(new Runnable() {
//				@Override
//				public void run() {
//					Observer o = null;
//					try {
//						Thread.sleep((int)Math.floor(Math.random()*5000));
//						o = new Observer("::1", 5683, );
//					} catch (InterruptedException e) {
//						o.clearRelations();
//					}
//					o.resourceRegistrationCLI();
//					while (true) {
//						try {
//							Thread.sleep(500);
//						} catch (InterruptedException e) {
//							o.clearRelations();
//						}
//					}
//				}
//			});
			String command = "java -jar Observer.java " + proxyIPv6 + " " + proxyPort + " " + String.valueOf(30000 + i) + " false true";
			try {
				Thread.sleep((int) Math.floor(Math.random() * 5000));
				Process proc = Runtime.getRuntime().exec(command);
				InputStream in = proc.getInputStream();
				InputStream err = proc.getErrorStream();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
