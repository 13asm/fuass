package fuass.model;

import java.util.ArrayList;
import java.util.List;

public class SkillGroup {

    private final String groupName;
    private final List<Skill> skills;

    public SkillGroup(String groupName) {
        this.groupName = groupName;
        skills = new ArrayList<>();
    }

    public String getGroupName() { return groupName; }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public Skill getSkillForPoints(int points) {
        Skill bestMatch = null;
        for (Skill skill : skills) {
            int threshold = skill.getPointsToActivate();
            if (threshold > 0 && points >= threshold) {
                if (bestMatch == null || threshold > bestMatch.getPointsToActivate()) {
                    bestMatch = skill;
                }
            } else if (threshold < 0 && points <= threshold) {
                if (bestMatch == null || threshold < bestMatch.getPointsToActivate()) {
                    bestMatch = skill;
                }
            }
        }
        return bestMatch;
    }
}