package anaws;

import java.sql.Timestamp;

public class Log {
	public static void debug(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ DEBUG ] "+text);
	}
	public static void info(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+ " [ "+module+" ] "+" [ INFO ] "+text);
	}
	public static void error(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ ERROR ] "+text);
	}
}
