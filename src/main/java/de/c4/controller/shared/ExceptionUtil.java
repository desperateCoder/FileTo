package main.java.de.c4.controller.shared;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
	public static String getStacktrace(Throwable t){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}
}
