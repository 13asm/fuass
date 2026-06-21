package fuass.model;

import java.util.HashMap;
import java.util.Map;

public class Armor {

    private final int id;
    private final String name;
    //private final String type;
    private final String gender;
    private final String hunterType;
    private final int hunterRank;
    private final int villageRank;
    //private final int rarity;
    private final int defense;
    private final int maxDefense;
    private final int fireRes;
    private final int waterRes;
    private final int thunderRes;
    private final int iceRes;
    private final int dragonRes;
    private final int slots;
    private final Map<String, Integer> skills;
    private final int price;
    private final Map<String, Integer> materialsA;
    private final Map<String, Integer> materialsB;
    private final boolean isPlate;
    private final boolean hasTorsoInc;

    // CSV column mapping:
    // id(0), name(1), type(2), gender(3), hunterType(4), hunterRank(5), villageRank(6), rarity(7),
    // defense(8), maxDefense(9), fireRes(10), waterRes(11), thunderRes(12), iceRes(13), dragonRes(14),
    // slots(15), skills(16-25, pairs of name+points),
    // price(26), materialsA(27-34, pairs of name+qty), materialsB(35-38, pairs of name+qty)

    public Armor(String type, String[] data) {
        id = Integer.parseInt(data[0]);
        name = data[1];
        //this.type = type;
        gender = data[3];
        hunterType = data[4];
        hunterRank = parseOrDefault(data[5], 9);
        villageRank = parseOrDefault(data[6], 9);
        //rarity = Integer.parseInt(data[7]);
        defense = Integer.parseInt(data[8]);
        maxDefense = Integer.parseInt(data[9]);
        fireRes = Integer.parseInt(data[10]);
        waterRes = Integer.parseInt(data[11]);
        thunderRes = Integer.parseInt(data[12]);
        iceRes = Integer.parseInt(data[13]);
        dragonRes = Integer.parseInt(data[14]);
        slots = Integer.parseInt(data[15]);

        Map<String, Integer> tempSkills = new HashMap<>();
        for (int i = 16; i < 26; i += 2) {
            if (!data[i].isEmpty()) {
                // Torso Inc have no value in the CSV,
                // this lets the armor predicate correctly identify them (values < 0).
                tempSkills.put(data[i], parseOrDefault(data[i + 1], -1));
            } else {
                break;
            }
        }
        skills = Map.copyOf(tempSkills);

        price = Integer.parseInt(data[26]);

        Map<String, Integer> tempMatsA = new HashMap<>();
        for (int i = 27; i < 35; i += 2) {
            if (!data[i].isEmpty() && !data[i + 1].isEmpty()) {
                tempMatsA.put(data[i], Integer.parseInt(data[i + 1]));
            } else {
                break;
            }
        }
        materialsA = Map.copyOf(tempMatsA);

        Map<String, Integer> tempMatsB = new HashMap<>();
        for (int i = 35; i < data.length; i += 2) {
            if (!data[i].isEmpty() && !data[i + 1].isEmpty()) {
                tempMatsB.put(data[i], Integer.parseInt(data[i + 1]));
            } else {
                break;
            }
        }
        materialsB = Map.copyOf(tempMatsB);

        isPlate = "plate".equals(type);
        hasTorsoInc = hasSkill("Torso Inc");
    }

    public Armor(String type, int slots) {
        id = -1;
        name = "any " + type + " with " + slots + " slots";
        //this.type = type;
        this.slots = slots;
        gender = null;
        hunterType = null;
        hunterRank = 0;
        villageRank = 0;
        //rarity = 0;
        defense = 0;
        maxDefense = 0;
        fireRes = 0;
        waterRes = 0;
        thunderRes = 0;
        iceRes = 0;
        dragonRes = 0;
        price = 0;
        skills = Map.of();
        materialsA = Map.of();
        materialsB = Map.of();
        isPlate = "plate".equals(type);
        hasTorsoInc = false;
    }

    // ---getters---

    public int getId()                          { return id; }
    public String getName()                     { return name; }
    //public String getType()                     { return type; }
    public String getGender()                   { return gender; }
    public String getHunterType()               { return hunterType; }
    public int getHunterRank()                  { return hunterRank; }
    public int getVillageRank()                 { return villageRank; }
    //public int getRarity()                      { return rarity; }
    public int getDefense()                     { return defense; }
    public int getMaxDefense()                  { return maxDefense; }
    public int getFireRes()                     { return fireRes; }
    public int getWaterRes()                    { return waterRes; }
    public int getThunderRes()                  { return thunderRes; }
    public int getIceRes()                      { return iceRes; }
    public int getDragonRes()                   { return dragonRes; }
    public int getSlots()                       { return slots; }
    public Map<String, Integer> getSkills()     { return skills; }
    public int getPrice()                       { return price; }
    public Map<String, Integer> getMaterialsA() { return materialsA; }
    public Map<String, Integer> getMaterialsB() { return materialsB; }
    public boolean isPlate()                    { return isPlate; }
    public boolean hasTorsoInc()                { return hasTorsoInc; }

    // ---methods---

    public boolean hasSkill(String skillPointsName) {
        return skills.containsKey(skillPointsName);
    }

    public int getSkillPoints(String skillPointsName) {
        return skills.getOrDefault(skillPointsName, 0);
    }

    private static int parseOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}