package fuass.logic;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {

    public static final int BAD_SKILL_THRESHOLD = -10;

    private Calculator() {}

    // ---methods---

    public static List<Decoration> validDecorations(List<Decoration> decorations, int[] slots) {
        List<Decoration> result = new ArrayList<>();
        for (Decoration deco : decorations) {
            int size = deco.getSize();
            for (int slot : slots) {
                if (size <= slot) {
                    result.add(deco);
                    break;
                }
            }
        }
        return result;
    }

    public static List<Decoration> usefulDecorations(List<Decoration> decorations,
                                                     Map<String, Integer> missingPoints) {
        List<Decoration> result = new ArrayList<>();
        for (Decoration deco : decorations) {
            for (Map.Entry<String, Integer> entry : missingPoints.entrySet()) {
                String skill = entry.getKey();
                int pointsNeeded = entry.getValue();
                int decoPoints = deco.getSkillPoints(skill);
                if ((pointsNeeded > 0 && decoPoints > 0) || (pointsNeeded < 0 && decoPoints < 0)) {
                    result.add(deco);
                    break;
                }
            }
        }
        return result;
    }

    public static boolean hasBadMixJewels(List<Skill> skills, List<Decoration> decorations) {
        for (Decoration deco : decorations) {
            if (deco.getSkills().size() > 1) {
                int beneficialCount = 0;
                int harmfulCount = 0;
                for (Skill skill : skills) {
                    String name = skill.getPointsName();
                    int required = skill.getPointsToActivate();
                    int points = deco.getSkillPoints(name);
                    if (deco.hasSkill(name)) {
                        // XOR sign check: result is negative only if the two values have opposite signs
                        boolean sameSign = (points ^ required) >= 0;
                        if (sameSign) beneficialCount++;
                        else harmfulCount++;
                    }
                }
                if (beneficialCount >= 1 && harmfulCount >= 1) return true;
            }
        }
        return false;
    }

    public static boolean hasGoodMixJewels(List<Skill> skills, List<Decoration> decorations) {
        for (Decoration deco : decorations) {
            if (deco.getSkills().size() > 1) {
                int beneficialCount = 0;
                for (Skill skill : skills) {
                    String name = skill.getPointsName();
                    int required = skill.getPointsToActivate();
                    int points = deco.getSkillPoints(name);
                    if (deco.hasSkill(name)) {
                        // XOR sign check: result is negative only if the two values have opposite signs
                        boolean sameSign = (points ^ required) >= 0;
                        if (sameSign) beneficialCount++;
                    }
                }
                if (beneficialCount >= 2) return true;
            }
        }
        return false;
    }

    public static double calculateBestEfficiency(List<Decoration> decos, Map<Integer, Double> efficiencyCache) {
        double bestEfficiency = 0.0;
        for (Decoration deco : decos) {
            double e = efficiencyCache.getOrDefault(deco.getId(), 0.0);
            if (e > bestEfficiency) bestEfficiency = e;
        }
        return bestEfficiency;
    }

    public static int minimumSlotsNeeded(Map<String, Integer> missingPoints, List<Decoration> decorations) {
        int totalSlots = 0;
        for (Map.Entry<String, Integer> entry : missingPoints.entrySet()) {
            String skill = entry.getKey();
            int pointsRequired = entry.getValue();
            if (pointsRequired == 0) continue;

            double bestEfficiency = 0.0;
            for (Decoration deco : decorations) {
                if (deco.hasSkill(skill)) {
                    int decoPoints = deco.getSkillPoints(skill);
                    if ((pointsRequired > 0 && decoPoints > 0) || (pointsRequired < 0 && decoPoints < 0)) {
                        double efficiency = Math.abs((double) decoPoints / deco.getSize());
                        if (efficiency > bestEfficiency) bestEfficiency = efficiency;
                    }
                }
            }

            if (bestEfficiency == 0.0) return Integer.MAX_VALUE;
            totalSlots += (int) Math.ceil(Math.abs((double) pointsRequired) / bestEfficiency);
        }
        return totalSlots;
    }

    public static int totalMissingPoints(Map<String, Integer> missingPoints) {
        int sum = 0;
        for (int value : missingPoints.values()) {
            sum += Math.abs(value);
        }
        return sum;
    }

    public static Map<String, Integer> applyTorsoPlatePoints(Map<String, Integer> points,
                                                             List<Decoration> decos, int torsoCounter) {
        Map<String, Integer> result = new HashMap<>(points);
        int multiplier = torsoCounter + 1;
        for (Decoration deco : decos) {
            for (Map.Entry<String, Integer> entry : deco.getSkills().entrySet()) {
                result.merge(entry.getKey(), entry.getValue() * multiplier, Integer::sum);
            }
        }
        return result;
    }

    public static Map<String, Integer> getPointsMap(List<Armor> armorCombo, List<Decoration> decoCombo,
                                                    List<Decoration> plateDecoCombo, int torsoCounter) {
        Map<String, Integer> result = new HashMap<>();

        if (torsoCounter > 0) {
            addSkillsFast(result, armorCombo.getFirst().getSkills(), torsoCounter + 1);
            for (Decoration deco : plateDecoCombo) {
                addSkillsFast(result, deco.getSkills(), torsoCounter + 1);
            }
        }

        for (int i = 0; i < armorCombo.size(); i++) {
            if (armorCombo.get(i) != null && !(i == 0 && torsoCounter > 0)) {
                addSkillsFast(result, armorCombo.get(i).getSkills(), 1);
            }
        }

        for (Decoration deco : decoCombo) {
            addSkillsFast(result, deco.getSkills(), 1);
        }

        return result;
    }

    private static void addSkillsFast(Map<String, Integer> target, Map<String, Integer> source, int multiplier) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.merge(entry.getKey(), entry.getValue() * multiplier, Integer::sum);
        }
    }

    public static boolean hasAnyBadSkill(Map<String, Integer> pointsMap) {
        for (int points : pointsMap.values()) {
            if (points <= BAD_SKILL_THRESHOLD) return true;
        }
        return false;
    }
}