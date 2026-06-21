package fuass.ui;

import fuass.model.Combination;
import fuass.model.Skill;
import fuass.model.SkillGroup;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ResultsBuilder {

    private static final int MAX_RESULTS  = 100;

    public JPanel buildResultsPanel(List<Combination> combinations,
                                    Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {

        JPanel panel = buildPanel();

        if (combinations == null || combinations.isEmpty()) {
            panel.add(plainLabel("0 Results"));
            return panel;
        }

        addHeader(panel, combinations.size());
        addCombinations(panel, combinations, allSkillGroups, selectedSkills);

        return panel;
    }

    // ---methods---

    private void addHeader(JPanel panel, int total) {
        String text = total + " Results" + (total > MAX_RESULTS ? " (showing first " + MAX_RESULTS + "):" : ":");
        panel.add(plainLabel(text));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JLabel plainLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        return label;
    }

    private void addCombinations(JPanel panel, List<Combination> combinations,
                                 Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {

        int limit = Math.min(combinations.size(), MAX_RESULTS);
        for (int i = 0; i < limit; i++) {
            panel.add(new CombinationPanel(combinations.get(i), i + 1, allSkillGroups, selectedSkills));
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        panel.setBackground(Color.WHITE);
        return panel;
    }
}