package fuass;

import fuass.io.DataStore;
import fuass.ui.GUI;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

public class Main {

    static void main() {
        try {
            applyLookAndFeel();
            applyGlobalFont();
            DataStore dataStore = new DataStore();
            SwingUtilities.invokeLater(() -> new GUI(dataStore));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            // replicate "metal" LookAndFeel JComboBox behavior
            UIManager.put("ComboBox.ancestorInputMap",
                    new MetalLookAndFeel().getDefaults().get("ComboBox.ancestorInputMap"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyGlobalFont() {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        for (Object key : UIManager.getDefaults().keySet()) {
            Object value = UIManager.get(key);
            if (value instanceof Font originalFont) {
                UIManager.put(key, font.deriveFont(originalFont.getStyle()));
            }
        }
    }
}
