package fuass.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Decoration {

    private final int id;
    private final String name;
    private final int hunterRank;
    private final int villageRank;
    //private final int rarity;
    private final int size;
    private final Map<String, Integer> skills;
    private final int price;
    private final Map<String, Integer> materialsA;
    private final Map<String, Integer> materialsB;

    // CSV column mapping:
    // id(0), name(1), hunterRank(2), villageRank(3), rarity(4), size(5),
    // skills(6-9, pairs of name+points),
    // price(10), matsA(11-18, pairs of name+qty), matsB(19-26, pairs of name+qty)

    public Decoration(String[] data) {
        id = Integer.parseInt(data[0]);
        name = data[1];
        hunterRank = parseOrDefault(data[2]);
        villageRank = parseOrDefault(data[3]);
        //rarity = Integer.parseInt(data[4]);
        size = Integer.parseInt(data[5]);

        Map<String, Integer> tempSkills = new HashMap<>();
        for (int i = 6; i < 10; i += 2) {
            if (!data[i].isEmpty() && !data[i + 1].isEmpty()) {
                tempSkills.put(data[i], Integer.parseInt(data[i + 1]));
            } else {
                break;
            }
        }
        skills = Map.copyOf(tempSkills);

        price = Integer.parseInt(data[10]);

        Map<String, Integer> tempMatsA = new HashMap<>();
        for (int i = 11; i < 19; i += 2) {
            if (!data[i].isEmpty() && !data[i + 1].isEmpty()) {
                tempMatsA.put(data[i], Integer.parseInt(data[i + 1]));
            } else {
                break;
            }
        }
        materialsA = Map.copyOf(tempMatsA);

        Map<String, Integer> tempMatsB = new HashMap<>();
        for (int i = 19; i < data.length; i += 2) {
            if (!data[i].isEmpty() && !data[i + 1].isEmpty()) {
                tempMatsB.put(data[i], Integer.parseInt(data[i + 1]));
            } else {
                break;
            }
        }
        materialsB = Map.copyOf(tempMatsB);
    }

    // ---getters---

    public int getId()                          { return id; }
    public String getName()                     { return name; }
    public int getHunterRank()                  { return hunterRank; }
    public int getVillageRank()                 { return villageRank; }
    //public int getRarity()                      { return rarity; }
    public int getSize()                        { return size; }
    public Map<String, Integer> getSkills()     { return skills; }
    public int getPrice()                       { return price; }
    public Map<String, Integer> getMaterialsA() { return materialsA; }
    public Map<String, Integer> getMaterialsB() { return materialsB; }

    // ---methods---

    public boolean hasSkill(String skillPointsName) {
        return skills.containsKey(skillPointsName);
    }

    public int getSkillPoints(String skillPointsName) {
        return skills.getOrDefault(skillPointsName, 0);
    }

    public double getEfficiency(List<Skill> targetSkills) {
        double total = 0.0;
        for (Skill skill : targetSkills) {
            String skillName = skill.getPointsName();
            if (!this.hasSkill(skillName)) continue;
            int required = skill.getPointsToActivate();
            int points = this.getSkillPoints(skillName);

            total += (required > 0 ? 1 : -1) * points;
        }
        return total / this.getSize();
    }

    private static int parseOrDefault(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 9;
        }
    }
}