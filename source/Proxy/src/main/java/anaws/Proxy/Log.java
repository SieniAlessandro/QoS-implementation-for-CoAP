package anaws.Proxy;

import java.sql.Timestamp;

public class Log {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
	public static void debug(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ " + ANSI_CYAN + " DEBUG " + ANSI_RESET + "] "+text);
	}
	public static void info(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+ " [ "+module+" ] "+" [ " + ANSI_GREEN + " INFO " + ANSI_RESET + "] "+text);
	}
	public static void error(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ " + ANSI_RED + " ERROR " + ANSI_RESET + "] "+text);
	}
}
