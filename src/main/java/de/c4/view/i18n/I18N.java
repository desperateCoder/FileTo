package main.java.de.c4.view.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.Settings;

public class I18N {

	private static Properties defaultProperties = new Properties();
	private static Properties i18nProperties = new Properties();

	static {
		InputStream inputStream = null;
		try {
			inputStream = I18N.class.getResourceAsStream("../resources/i18n/en_en.properties");
			if (inputStream != null) {
				defaultProperties.load(inputStream);
			}
			// TODO prevent path hijacking (filter .)
			if (Settings.INSTANCE.get(Settings.LANGUAGE) != null) {
				inputStream = I18N.class.getResourceAsStream(
						"../resources/i18n/" + Settings.INSTANCE.get(Settings.LANGUAGE) + ".properties");
				if (inputStream != null) {
					i18nProperties.load(inputStream);
				}
			}
		} catch (IOException e) {
			Log.error(e.getMessage());
		} finally {
			try {
				inputStream.close();
			} catch (IOException | NullPointerException e) {
				Log.error(e.getMessage());
			}
		}
	}

	public static String get(String key) {
		if (i18nProperties.containsKey(key)) {
			return i18nProperties.getProperty(key);
		} else if (defaultProperties.containsKey(key)) {
			return defaultProperties.getProperty(key);
		} else {
			Log.error("Missing Language Key: " + key);
			return "";
		}
	}
}
