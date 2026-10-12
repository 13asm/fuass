package fuass.io;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches game icons from classpath resources.
 * Icons are loaded lazily on first access to minimize startup time.
 *
 * <p>All UI classes share the single instance returned by {@link #shared()},
 * so every icon is decoded and scaled only once for the whole application.
 */
public class IconLoader {

    private static final int ICON_SIZE = 12;

    /** Icon keys in display order: defense, then the elemental resistances. */
    private static final String[] ICON_KEYS = {
            "baseDefense", "maxDefense", "fire", "water", "thunder", "ice", "dragon"
    };

    private static final IconLoader SHARED = new IconLoader();

    private final Map<String, ImageIcon> icons = new HashMap<>();

    private IconLoader() {}

    /** The one loader used across the UI. */
    public static IconLoader shared() {
        return SHARED;
    }

    /** Icon keys in the order the matching stat values are listed. */
    public static List<String> keys() {
        return List.of(ICON_KEYS);
    }

    private ImageIcon loadIcon(String path) {
        URL resource = getClass().getResource("/" + path);

        if (resource == null) {
            System.err.println("Icon resource not found: " + path);
            return null;
        }

        ImageIcon icon = new ImageIcon(resource);
        Image scaled = icon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);

        return new ImageIcon(scaled);
    }

    public ImageIcon getIcon(String name) {
        return icons.computeIfAbsent(name, key -> loadIcon("icons/" + key + ".png"));
    }
}
