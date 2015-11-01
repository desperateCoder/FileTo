package main.java.de.c4.view.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.Settings;

public class I18N {

	private static Properties i18nProperties = new Properties();

	static {
		initialize();
	}

	private static void initialize() {
		InputStream inputStream = null;
		try {
			
			// TODO prevent path hijacking (filter .)
			String language = Settings.INSTANCE.get(Settings.LANGUAGE);
			if (language != null) {
				inputStream = I18N.class.getResourceAsStream(
						"resources/" + language + ".properties");
				if (inputStream != null) {
					i18nProperties.load(inputStream);
					Log.debug("Language loaded: ");
					try {
						inputStream.close();
					} catch (IOException | NullPointerException e) {
						Log.error(e.getMessage());
					}
					return;
				} else Log.debug("Languagepack \""+language+"\" not found!");
			}
			
			inputStream = I18N.class.getResourceAsStream("resources/en_en.properties");
			if (inputStream != null) {
				i18nProperties.load(inputStream);
			} else Log.error("Could not load default language!");
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
		} else {
			Log.debug("Missing Language Key: " + key);
			return "";
		}
	}
}
