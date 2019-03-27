/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 ******************************************************************************/



import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;



public class ProxySubject{

	public static void main(String args[]) {
		CacheTable cache = new CacheTable();
		SensorNode s = new SensorNode("1",1000);
		SensorNode s2 = new SensorNode("2",1000);
		SensorNode s3 = new SensorNode("3",1000);
		Registrator r = new Registrator();
		Registration reg = new Registration(s,"temp",false);
		Registration reg2 = new Registration(s2,"temp",false);
		Registration reg3 = new Registration(s3,"temp",false);
		r.newRegistration(reg);
		r.newRegistration(reg2);
		r.newRegistration(reg3);
		new Generator(reg,cache).start();
		new Generator(reg2,cache).start();
		new Generator(reg3,cache).start();
		//r.newRegistration(r4);
		//r.newRegistration(r5);
		//cache.insertData(new SensorData(s,1.0,"Temperature",10,true));
		/*
		Updater u = new Updater(cache);
		u.start();
		try{
			Thread.sleep(1000);
			cache.insertData(new SensorData(s,1.0,"Temperature",10,false));
			Thread.sleep(1000);
				cache.insertData(new SensorData(s,2.0,"Temperature",20,false));
		}catch(Exception e){}
		*/
	}
}
