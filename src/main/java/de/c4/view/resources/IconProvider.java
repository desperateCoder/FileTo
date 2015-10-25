package main.java.de.c4.view.resources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import main.java.de.c4.controller.shared.ExceptionUtil;

import com.esotericsoftware.minlog.Log;

public class IconProvider {
	public static BufferedImage getImage(EIcons icon) {
		InputStream stream = IconProvider.class.getResourceAsStream(icon.getPath());
		try {
			return ImageIO.read(stream);
		} catch (IOException e) {
			Log.error("Datei \"" + icon.getPath() + "\" nicht gefunden.");
			Log.error(ExceptionUtil.getStacktrace(e));
		} catch (Exception e) {
			Log.error("Fehler beim lesen der Datei.");
			Log.error(ExceptionUtil.getStacktrace(e));
		}
		return null;
	}
	
	public static URL getImageAsURL(EIcons icon) {
		return IconProvider.class.getResource("./"+icon.getPath());
	}
}
