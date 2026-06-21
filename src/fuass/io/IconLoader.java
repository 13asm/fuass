package fuass.io;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class IconLoader {

    private static final int ICON_SIZE = 12;
    private static final String[] ICON_KEYS = {
            "baseDefense", "maxDefense", "fire", "water", "thunder", "ice", "dragon"
    };

    private final Map<String, ImageIcon> icons = new HashMap<>();

    public IconLoader() {
        loadIcons();
    }

    private void loadIcons() {
        for (String key : ICON_KEYS) {
            icons.put(key, loadIcon("icons/" + key + ".png"));
        }
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
        return icons.get(name);
    }
}