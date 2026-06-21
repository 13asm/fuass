package fuass.logic;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * an item A dominates item B if A is at least as good as B
 * in every relevant dimension and strictly better in at least one.
 * items that are dominated by any other candidate are removed from consideration.
 */

public class DominanceFilter {

    private DominanceFilter() {}

    public static List<Armor> armorParetoFilter(List<Armor> allCandidates, List<Skill> selectedSkills) {
        List<Armor> result = new ArrayList<>();

        for (Armor candidate1 : allCandidates) {
            List<Integer> scores1 = getArmorScores(candidate1, selectedSkills);
            boolean dominated = false;

            for (Armor candidate2 : allCandidates) {
                if (candidate1 == candidate2) continue;

                List<Integer> scores2 = getArmorScores(candidate2, selectedSkills);
                if (dominatesArmor(scores2, scores1, selectedSkills)) {
                    dominated = true;
                    break;
                }
            }

            if (!dominated) {
                result.add(candidate1);
            }
        }
        return result;
    }

    private static List<Integer> getArmorScores(Armor armor, List<Skill> skills) {
        List<Integer> scores = new ArrayList<>();
        for (Skill skill : skills) {
            String name = skill.getPointsName();
            scores.add(armor.hasSkill(name) ? armor.getSkillPoints(name) : 0);
        }
        scores.add(armor.getSlots());
        scores.add(armor.getDefense());
        scores.add(armor.getId());
        return scores;
    }

    private static boolean dominatesArmor(List<Integer> scoresA, List<Integer> scoresB, List<Skill> skills) {
        boolean strictlyBetter = false;

        for (int i = 0; i < skills.size(); i++) {
            int a = scoresA.get(i);
            int b = scoresB.get(i);

            if (skills.get(i).getPointsToActivate() > 0) {
                if (a < b) return false;
                if (a > b) strictlyBetter = true;
            } else {
                if (a > b) return false;
                if (a < b) strictlyBetter = true;
            }
        }

        int slotsA = scoresA.get(skills.size());
        int slotsB = scoresB.get(skills.size());
        if (slotsA < slotsB) return false;
        if (slotsA > slotsB) strictlyBetter = true;

        if (strictlyBetter) return true;

        // tiebreaker 1: defense
        int defenseA = scoresA.get(skills.size() + 1);
        int defenseB = scoresB.get(skills.size() + 1);
        if (defenseA < defenseB) return false;
        if (defenseA > defenseB) return true;

        // tiebreaker 2: id
        int idA = scoresA.get(skills.size() + 2);
        int idB = scoresB.get(skills.size() + 2);
        return idA < idB;
    }

    public static List<Decoration> decoParetoFilter(List<Decoration> allCandidates, List<Skill> selectedSkills) {
        List<Decoration> result = new ArrayList<>();

        for (Decoration candidate1 : allCandidates) {
            List<Integer> scores1 = getDecoScores(candidate1, selectedSkills);
            boolean dominated = false;

            for (Decoration candidate2 : allCandidates) {
                if (candidate1 == candidate2) continue;

                List<Integer> scores2 = getDecoScores(candidate2, selectedSkills);
                if (dominatesDeco(scores2, scores1, selectedSkills)) {
                    dominated = true;
                    break;
                }
            }

            if (!dominated) {
                result.add(candidate1);
            }
        }
        return result;
    }

    private static List<Integer> getDecoScores(Decoration deco, List<Skill> skills) {
        List<Integer> scores = new ArrayList<>();
        for (Skill skill : skills) {
            String name = skill.getPointsName();
            scores.add(deco.hasSkill(name) ? deco.getSkillPoints(name) : 0);
        }
        scores.add(deco.getSize());
        scores.add(deco.getId());
        return scores;
    }

    private static boolean dominatesDeco(List<Integer> scoresA, List<Integer> scoresB, List<Skill> skills) {
        boolean strictlyBetter = false;
        int sizeA = scoresA.get(skills.size());
        int sizeB = scoresB.get(skills.size());

        for (int i = 0; i < skills.size(); i++) {
            int pointsA = scoresA.get(i);
            int pointsB = scoresB.get(i);

            double effA = (double) pointsA / sizeA;
            double effB = (double) pointsB / sizeB;

            if (skills.get(i).getPointsToActivate() > 0) {
                if (effA < effB) return false;
                if (effA > effB) strictlyBetter = true;
            } else {
                if (effA > effB) return false;
                if (effA < effB) strictlyBetter = true;
            }
        }

        // smaller size is better: fits in more armor slots
        if (sizeA > sizeB) return false;
        if (sizeA < sizeB) strictlyBetter = true;

        if (strictlyBetter) return true;

        // tiebreaker: id
        int idA = scoresA.get(skills.size() + 1);
        int idB = scoresB.get(skills.size() + 1);
        return idA < idB;
    }
}