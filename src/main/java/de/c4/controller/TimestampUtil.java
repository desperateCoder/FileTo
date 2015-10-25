package main.java.de.c4.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampUtil {

	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
	
	public static String getCurrentTimestamp(){
		return FORMAT.format(new Date());
	}

}
