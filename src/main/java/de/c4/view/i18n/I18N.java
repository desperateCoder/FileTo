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
			// load fallback
			String language = Settings.INSTANCE.get(Settings.LANGUAGE);
			inputStream = I18N.class.getResourceAsStream("resources/en_en.properties");
			if (inputStream != null) {
				i18nProperties.load(inputStream);
				try {
					inputStream.close();
				} catch (IOException | NullPointerException e) {
					Log.error(e.getMessage());
				}
				if ("en_en".equals(language)) {
					return;
				}
			} else Log.error("Could not load default language!");
			
			// TODO prevent path hijacking (Regex has to match letter-letter-underscore-letter-letter)
			if (language != null) {
				inputStream = I18N.class.getResourceAsStream(
						"resources/" + language + ".properties");
				if (inputStream != null) {
					Properties p = new Properties();
					p.load(inputStream);
					for (Object key : p.keySet()) {
						i18nProperties.put(key, p.get(key));
					}
					Log.debug("Language loaded: "+language);
					try {
						inputStream.close();
					} catch (IOException | NullPointerException e) {
						Log.error(e.getMessage());
					}
				} else Log.debug("Languagepack \""+language+"\" not found!");
			}
			
		} catch (IOException e) {
			Log.error(e.getMessage());
		} finally {
			try {
				if (inputStream!= null) {
					inputStream.close();
				}
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
