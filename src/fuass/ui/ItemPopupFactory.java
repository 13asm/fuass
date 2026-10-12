package fuass.ui;

import fuass.io.IconLoader;
import fuass.model.Armor;
import fuass.model.Decoration;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Builds the detail popups shared by the search result rows and the
 * Advanced Search checklist, so both places show exactly the same data.
 * The stat widgets live here too: the combination summary reuses the same
 * icons and values as the popups, see {@link #statsRow(int[])}.
 */
public final class ItemPopupFactory {

    private static final IconLoader ICON_LOADER = IconLoader.shared();

    /** Stat icons in display order, taken from {@link IconLoader#keys()}. */
    private static final List<String> NAME_ICONS = IconLoader.keys();

    private ItemPopupFactory() {}

    public static JPopupMenu forArmor(Armor armor) {
        JPopupMenu popup = new JPopupMenu();

        popup.add(popupLabel(armor.getName()));
        popup.addSeparator();
        appendSlots(popup, armor.getSlots());
        appendArmorPoints(popup, armor);

        // id < 0 identifies virtual placeholder armors, these have no stats or mats to display
        if (armor.getId() >= 0) {
            popup.addSeparator();
            appendStats(popup, new int[]{
                    armor.getDefense(), armor.getMaxDefense(),
                    armor.getFireRes(),  armor.getWaterRes(), armor.getThunderRes(),
                    armor.getIceRes(),   armor.getDragonRes()
            });
            if (!armor.getMaterialsA().isEmpty()) {
                popup.addSeparator();
                popup.add(popupLabel(armor.getPrice() + "z"));
                appendMaterials(popup, armor.getMaterialsA());
            }
            if (!armor.getMaterialsB().isEmpty()) {
                popup.addSeparator();
                popup.add(popupLabel(armor.getPrice() + "z"));
                appendMaterials(popup, armor.getMaterialsB());
            }
        }

        return popup;
    }

    public static JPopupMenu forDecoration(Decoration deco) {
        JPopupMenu popup = new JPopupMenu();

        popup.add(popupLabel(deco.getName()));
        popup.addSeparator();
        appendSlots(popup, deco.getSize());
        popup.addSeparator();
        for (Map.Entry<String, Integer> entry : deco.getSkills().entrySet()) {
            popup.add(popupLabel(formatSkillEntry(entry.getKey(), entry.getValue())));
        }
        popup.addSeparator();
        popup.add(popupLabel(deco.getPrice() + "z"));
        appendMaterials(popup, deco.getMaterialsA());
        if (!deco.getMaterialsB().isEmpty()) {
            popup.addSeparator();
            popup.add(popupLabel(deco.getPrice() + "z"));
            appendMaterials(popup, deco.getMaterialsB());
        }

        return popup;
    }

    // ---shared helpers---

    private static void appendArmorPoints(JPopupMenu popup, Armor armor) {
        Map<String, Integer> skills = armor.getSkills();
        if (skills.isEmpty()) return;
        popup.addSeparator();
        if (armor.hasTorsoInc()) {
            popup.add(popupLabel("Torso Inc"));
        } else {
            for (Map.Entry<String, Integer> entry : skills.entrySet()) {
                popup.add(popupLabel(formatSkillEntry(entry.getKey(), entry.getValue())));
            }
        }
    }

    private static void appendSlots(JPopupMenu popup, int size) {
        String slots = switch (size) {
            case 1  -> "o - -";
            case 2  -> "o o -";
            case 3  -> "o o o";
            default -> "- - -";
        };
        popup.add(popupLabel(slots));
    }

    private static void appendMaterials(JPopupMenu popup, Map<String, Integer> materials) {
        for (Map.Entry<String, Integer> entry : materials.entrySet()) {
            popup.add(popupLabel("x" + String.format("%-2d %s", entry.getValue(), entry.getKey())));
        }
    }

    /**
     * Horizontal row of stat values with icons, used by the combination
     * summary. The detail popups stack the very same values vertically via
     * {@link #appendStats}, so both show identical data and icons.
     */
    static JPanel statsRow(int[] values) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setOpaque(true);

        for (int i = 0; i < NAME_ICONS.size(); i++) {
            JLabel label = statLabel(values[i], NAME_ICONS.get(i), false);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            panel.add(label);
        }
        return panel;
    }

    private static void appendStats(JPopupMenu popup, int[] stats) {
        for (int i = 0; i < NAME_ICONS.size(); i++) {
            JLabel label = statLabel(stats[i], NAME_ICONS.get(i), true);
            label.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
            popup.add(label);
        }
    }

    /** One stat value with its icon; signed renders the "+"/"-" used in popups. */
    private static JLabel statLabel(int value, String iconKey, boolean signed) {
        String text = signed ? " " + String.format("%+d", value) : String.valueOf(value);
        return new JLabel(text, ICON_LOADER.getIcon(iconKey), JLabel.LEFT);
    }

    static JLabel popupLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
        return label;
    }

    static String formatSkillEntry(String name, int points) {
        return String.format("%+-3d %s", points, name);
    }
}