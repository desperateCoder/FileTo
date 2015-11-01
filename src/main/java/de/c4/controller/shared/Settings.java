package main.java.de.c4.controller.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.esotericsoftware.minlog.Log;

public class Settings {
	
	public static final Settings INSTANCE = new Settings(true);
	
	public static final String CONTACT_NAME = "contactName";
	public static final String CONTACT_ONLINE_STATE = "onlineState";
	public static final String LOOK_AND_FEEL = "lookAndFeel";
	public static final String LANGUAGE = "language";
	
	
	private static final String SETTINGS_CONF = "./.fileto/settings.conf";
	private Properties prop = null;

	public Settings() {
		this(false);
	}
	private Settings(boolean isInternal) {
		if (!isInternal) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		InputStream input = null;
		try {
			prop =  new Properties();
			input = new FileInputStream(new File(SETTINGS_CONF));
			prop.load(input);
		} catch (IOException ex) {
			Log.debug(ExceptionUtil.getStacktrace(ex));
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					Log.debug(ExceptionUtil.getStacktrace(e));
				}
			}
		}
	}
	public String get(String key) {
		return prop.getProperty(key);
	}
	public void set(String key, String value) {
		prop.setProperty(key, value);
	}
	public void save() {
		OutputStream os = null;
		try {
			os = new FileOutputStream(SETTINGS_CONF);
			prop.store(os, "");
		} catch (FileNotFoundException e) {
			Log.error(ExceptionUtil.getStacktrace(e));
		} catch (IOException e) {
			Log.error(ExceptionUtil.getStacktrace(e));
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					Log.error(ExceptionUtil.getStacktrace(e));
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		Settings.INSTANCE.set(CONTACT_NAME, "Artur Dawtjan");
		Settings.INSTANCE.set(CONTACT_ONLINE_STATE, "1");
		Settings.INSTANCE.save();
	}
}
