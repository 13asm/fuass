package fuass.ui;

import fuass.io.IconLoader;
import fuass.model.*;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

public class CombinationPanel extends JPanel {

    private static final IconLoader ICON_LOADER = new IconLoader();
    private static final String[] NAME_ICONS =
            {"baseDefense", "maxDefense", "fire", "water", "thunder", "ice", "dragon"};

    private final Combination combo;
    private final int number;

    public CombinationPanel(Combination combo, int number,
                            Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {
        this.combo  = combo;
        this.number = number;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setBackground(Color.WHITE);

        buildContent(allSkillGroups, selectedSkills);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    // ---panel content from top to bottom---

    private void buildContent(Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {
        add(new JSeparator());
        addGap();
        addHeaderRow();
        addGap();
        addArmorRows();
        addGap();
        addDecorationRows();
        addGap();
        addRow(" " + combo.getFreeSlots() + " Free Slots");
        addGap();
        addExtraSkillRows(allSkillGroups, selectedSkills);
        addStatsRows();
    }

    private void addRow(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        add(label);
    }

    private void addHeaderRow() {
        String text = " Combination #" + number + " (id: " + combo.getId() + ")";
        addInteractiveRow(text, Font.BOLD, buildHeaderPopup());
    }

    private void addArmorRows() {
        for (Armor armor : combo.getArmorPieces()) {
            if (armor != null) {
                addInteractiveRow(" - " + armor.getName(), Font.PLAIN, buildArmorPopup(armor));
            } else {
                addRow(" - empty");
            }
        }
    }

    private void addDecorationRows() {
        for (Map.Entry<Decoration, Integer> entry : combo.getDecorationsMap().entrySet()) {
            String text = " - x" + entry.getValue() + " " + entry.getKey().getName();
            addInteractiveRow(text, Font.PLAIN, buildDecoPopup(entry.getKey()));
        }
    }

    private void addExtraSkillRows(Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {
        List<Skill> extras = combo.findExtraSkills(allSkillGroups, selectedSkills);
        if (extras.isEmpty()) return;

        addRow(" Extra skills:");
        for (Skill skill : extras) {
            addRow(" - " + skill.getName());
        }
        addGap();
    }

    private void addInteractiveRow(String text, int fontStyle, JPopupMenu popup) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(fontStyle));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        add(label);

        Color hoverBackground = UIManager.getColor("List.selectionBackground");
        Color hoverForeground = UIManager.getColor("List.selectionForeground");
        Color normalForeground = label.getForeground();

        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!popup.isVisible()) {
                    label.setBackground(hoverBackground);
                    label.setForeground(hoverForeground);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!popup.isVisible()) {
                    label.setBackground(Color.WHITE);
                    label.setForeground(normalForeground);
                }
            }
            @Override public void mousePressed(MouseEvent e) {
                label.setBackground(Color.WHITE);
                label.setForeground(normalForeground);
                popup.show(e.getComponent(), e.getX() - 2, e.getY() - 2);
            }
        });

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                label.setBackground(hoverBackground);
                label.setForeground(hoverForeground);
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                label.setBackground(Color.WHITE);
                label.setForeground(normalForeground);
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {
                label.setBackground(Color.WHITE);
                label.setForeground(normalForeground);
            }
        });
    }

    private void addStatsRows() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setOpaque(true);

        int[] values = {
                combo.getDefense(), combo.getMaxDefense(),
                combo.getFireRes(), combo.getWaterRes(), combo.getThunderRes(),
                combo.getIceRes(), combo.getDragonRes()
        };

        for (int i = 0; i < NAME_ICONS.length; i++) {
            JLabel label = new JLabel(String.valueOf(values[i]),
                    ICON_LOADER.getIcon(NAME_ICONS[i]), JLabel.LEFT);
            label.setFont(label.getFont().deriveFont(Font.PLAIN));
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            panel.add(label);
        }
        add(panel);
    }

    // ---armor popup---

    private JPopupMenu buildHeaderPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(popupLabel("id: " + combo.getId()));
        popup.addSeparator();
        Map<String, Integer> pointsMap = combo.getPointsMap();
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            popup.add(popupLabel(formatSkillEntry(entry.getKey(), entry.getValue())));
        }
        return popup;
    }

    private JPopupMenu buildArmorPopup(Armor armor) {
        JPopupMenu popup = new JPopupMenu();

        popup.add(popupLabel(armor.getName()));
        popup.addSeparator();
        appendSlotsPopup(popup, armor.getSlots());
        appendArmorPointsPopup(popup, armor);

        // id < 0 identifies virtual placeholder armors, these have no stats or mats to display
        if (armor.getId() >= 0) {
            popup.addSeparator();
            appendStatsPopup(popup, new int[]{
                    armor.getDefense(), armor.getMaxDefense(),
                    armor.getFireRes(),  armor.getWaterRes(), armor.getThunderRes(),
                    armor.getIceRes(),   armor.getDragonRes()
            });
            if (!armor.getMaterialsA().isEmpty()) {
                popup.addSeparator();
                popup.add(popupLabel(armor.getPrice() + "z"));
                for (Map.Entry<String, Integer> entry : armor.getMaterialsA().entrySet()) {
                    popup.add(popupLabel("x" + String.format("%-2d %s", entry.getValue(), entry.getKey())));
                }
            }
            if (!armor.getMaterialsB().isEmpty()) {
                popup.addSeparator();
                popup.add(popupLabel(armor.getPrice() + "z"));
                for (Map.Entry<String, Integer> entry : armor.getMaterialsB().entrySet()) {
                    popup.add(popupLabel("x" + String.format("%-2d %s", entry.getValue(), entry.getKey())));
                }
            }
        }

        return popup;
    }

    private void appendArmorPointsPopup(JPopupMenu popup, Armor armor) {
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

    // ---decoration popup---

    private JPopupMenu buildDecoPopup(Decoration deco) {
        JPopupMenu popup = new JPopupMenu();

        popup.add(popupLabel(deco.getName()));
        popup.addSeparator();
        appendSlotsPopup(popup, deco.getSize());
        popup.addSeparator();
        for (Map.Entry<String, Integer> entry : deco.getSkills().entrySet()) {
            popup.add(popupLabel(formatSkillEntry(entry.getKey(), entry.getValue())));
        }
        popup.addSeparator();
        popup.add(popupLabel(deco.getPrice() + "z"));
        for (Map.Entry<String, Integer> entry : deco.getMaterialsA().entrySet()) {
            popup.add(popupLabel("x" + String.format("%-2d %s", entry.getValue(), entry.getKey())));
        }
        if (!deco.getMaterialsB().isEmpty()) {
            popup.addSeparator();
            popup.add(popupLabel(deco.getPrice() + "z"));
            for (Map.Entry<String, Integer> entry : deco.getMaterialsB().entrySet()) {
                popup.add(popupLabel("x" + String.format("%-2d %s", entry.getValue(), entry.getKey())));
            }
        }

        return popup;
    }

    // ---shared helpers---

    private void appendSlotsPopup(JPopupMenu popup, int size) {
        String slots = switch (size) {
            case 1  -> "o - -";
            case 2  -> "o o -";
            case 3  -> "o o o";
            default -> "- - -";
        };
        popup.add(popupLabel(slots));
    }

    private void appendStatsPopup(JPopupMenu popup, int[] stats) {
        for (int i = 0; i < NAME_ICONS.length; i++) {
            JLabel label = new JLabel(" " + String.format("%+d", stats[i]),
                    ICON_LOADER.getIcon(NAME_ICONS[i]), JLabel.LEFT);
            label.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
            popup.add(label);
        }
    }

    private JLabel popupLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
        return label;
    }

    private String formatSkillEntry(String name, int points) {
        return String.format("%+-3d %s", points, name);
    }

    private void addGap() {
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
}