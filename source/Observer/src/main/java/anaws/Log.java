package anaws;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ " + ANSI_YELLOW+ " DEBUG " + ANSI_RESET + "] "+text);
	}
	public static void info(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+ " [ "+module+" ] "+" [ " + ANSI_GREEN + " INFO " + ANSI_RESET + "] "+text);
	}
	public static void error(String module,String text) {
		System.out.println(" [ "+new Timestamp(System.currentTimeMillis())+" ] "+" [ "+module+" ] "+" [ " + ANSI_RED + " ERROR " + ANSI_RESET + "] "+text);
	}
	public static void LogOnFile(String Filename, String value) {
		debug("Log","Apertura file");
		File file = new File(Filename);
		value = LocalDateTime.now().getMinute()+":"+LocalDateTime.now().getSecond()+","+value;
		try {
			if(file.exists()) {
				//If file Exists append to those file
				debug("Log","Scrittura sul file");
				BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
				writer.newLine();
				writer.write(value);
				writer.close();
				debug("Log","Fine Scrittura sul file");
			}else {
				//Otherwise create new file and write the record
				file.createNewFile();
				debug("Log","Scrittura sul file");
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write("Time,IPAddress,Value,Type,Critic,Observe");
				writer.newLine();
				writer.write(value);
				writer.close();
				debug("Log","Fine Scrittura sul file");
			}
		
		}catch(IOException ex) {ex.printStackTrace();}
	}
}