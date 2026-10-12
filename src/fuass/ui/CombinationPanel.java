package fuass.ui;

import fuass.model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Displays a single armor combination with interactive popup details.
 * Shows armor pieces, decorations, extra skills, and stats with icons.
 */
public class CombinationPanel extends JPanel {

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
        addInteractiveRow(text, Font.BOLD, this::buildHeaderPopup);
    }

    private void addArmorRows() {
        for (Armor armor : combo.getArmorPieces()) {
            if (armor != null) {
                addInteractiveRow(" - " + armor.getName(), Font.PLAIN,
                        () -> ItemPopupFactory.forArmor(armor));
            } else {
                addRow(" - empty");
            }
        }
    }

    private void addDecorationRows() {
        for (Map.Entry<Decoration, Integer> entry : combo.getDecorationsMap().entrySet()) {
            String text = " - x" + entry.getValue() + " " + entry.getKey().getName();
            addInteractiveRow(text, Font.PLAIN,
                () -> ItemPopupFactory.forDecoration(entry.getKey()));
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

    private void addInteractiveRow(String text, int fontStyle, Supplier<JPopupMenu> popupSupplier) {
        add(new PopupLabel(text, fontStyle, popupSupplier));
    }

    private void addStatsRows() {
        int[] values = {
                combo.getDefense(), combo.getMaxDefense(),
                combo.getFireRes(), combo.getWaterRes(), combo.getThunderRes(),
                combo.getIceRes(), combo.getDragonRes()
        };
        add(ItemPopupFactory.statsRow(values));
    }

    // ---popups---

    private JPopupMenu buildHeaderPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(ItemPopupFactory.popupLabel("id: " + combo.getId()));
        popup.addSeparator();
        Map<String, Integer> pointsMap = combo.getPointsMap();
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            popup.add(ItemPopupFactory.popupLabel(ItemPopupFactory.formatSkillEntry(entry.getKey(), entry.getValue())));
        }
        return popup;
    }

    private void addGap() {
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
}