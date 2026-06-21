package fuass.model;

import java.util.*;
import java.util.function.ToIntFunction;

public class Combination {

    private int id;
    private final List<Armor> armors;
    private final List<Decoration> decorations;
    private final List<Decoration> plateDecorations;
    private final List<Decoration> totalDecorations;
    private final int torsoCounter;
    private final int freeSlots;
    private final int defense;
    private final int maxDefense;
    private final int fireRes;
    private final int waterRes;
    private final int thunderRes;
    private final int iceRes;
    private final int dragonRes;
    private int extraSkills;

    public Combination(List<Armor> armors, List<Decoration> decorations, List<Decoration> plateDecorations,
                       int weaponSlots, int torsoCounter) {

        List<Armor> mutableArmors = new ArrayList<>(armors);
        // the search engine places the plate at index 0
        // swap restores natural in-game order: helmet(0), plate(1), gauntlets(2), waist(3), leggings(4)
        Collections.swap(mutableArmors, 0, 1);
        this.armors = Collections.unmodifiableList(mutableArmors);

        this.decorations = decorations != null ? List.copyOf(decorations) : List.of();
        this.plateDecorations = plateDecorations != null ? List.copyOf(plateDecorations) : List.of();

        totalDecorations = new ArrayList<>();
        totalDecorations.addAll(this.decorations);
        totalDecorations.addAll(this.plateDecorations);

        if (torsoCounter < 0) {
            int count = 0;
            for (Armor armor : armors) {
                if (armor != null && armor.hasTorsoInc()) {
                    count++;
                }
            }
            this.torsoCounter = count;
        } else {
            this.torsoCounter = torsoCounter;
        }

        extraSkills = 0;
        freeSlots = sumStat(Armor::getSlots) + weaponSlots - countDecoSlots();
        defense = sumStat(Armor::getDefense);
        maxDefense = sumStat(Armor::getMaxDefense);
        fireRes = sumStat(Armor::getFireRes);
        waterRes = sumStat(Armor::getWaterRes);
        thunderRes = sumStat(Armor::getThunderRes);
        iceRes = sumStat(Armor::getIceRes);
        dragonRes = sumStat(Armor::getDragonRes);
    }

    // ---getters---

    public int getId()                       { return id; }
    public List<Armor> getArmorPieces()      { return armors; }
    //public List<Decoration> getDecorations() { return decorations; }
    public int getExtraSkills()              { return extraSkills; }
    public int getFreeSlots()                { return freeSlots; }
    public int getDefense()                  { return defense; }
    public int getMaxDefense()               { return maxDefense; }
    public int getFireRes()                  { return fireRes; }
    public int getWaterRes()                 { return waterRes; }
    public int getThunderRes()               { return thunderRes; }
    public int getIceRes()                   { return iceRes; }
    public int getDragonRes()                { return dragonRes; }

    // ---methods---

    public void setId(int id) { this.id = id; }

    private int sumStat(ToIntFunction<Armor> getter) {
        int total = 0;
        for (Armor armor : armors) {
            if (armor != null) {
                total += getter.applyAsInt(armor);
            }
        }
        return total;
    }

    public Map<Decoration, Integer> getDecorationsMap() {
        Map<Decoration, Integer> result = new HashMap<>();
        for (Decoration deco : totalDecorations) {
            result.merge(deco, 1, Integer::sum);
        }
        return result;
    }

    private int countDecoSlots() {
        int total = 0;
        for (Decoration deco : totalDecorations) {
            total += deco.getSize();
        }
        return total;
    }

    public Map<String, Integer> getPointsMap() {
        Map<String, Integer> result = new HashMap<>();

        for (Armor armor : armors) {
            if (armor == null) continue;

            if (armor.isPlate() && torsoCounter > 0) {
                for (Map.Entry<String, Integer> entry : armor.getSkills().entrySet()) {
                    result.merge(entry.getKey(), entry.getValue() * (torsoCounter + 1), Integer::sum);
                }
            } else if (!armor.hasTorsoInc()) {
                for (Map.Entry<String, Integer> entry : armor.getSkills().entrySet()) {
                    result.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
        }

        for (Decoration deco : decorations) {
            for (Map.Entry<String, Integer> entry : deco.getSkills().entrySet()) {
                result.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        if (torsoCounter > 0) {
            for (Decoration deco : plateDecorations) {
                for (Map.Entry<String, Integer> entry : deco.getSkills().entrySet()) {
                    result.merge(entry.getKey(), entry.getValue() * (torsoCounter + 1), Integer::sum);
                }
            }
        }
        return result;
    }

    public List<Skill> findExtraSkills(Map<String, SkillGroup> allSkillGroups, List<Skill> selectedSkills) {
        List<Skill> result = new ArrayList<>();
        Map<String, Integer> pointsMap = getPointsMap();

        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            SkillGroup group = allSkillGroups.get(entry.getKey());
            if (group == null) continue;
            Skill skill = group.getSkillForPoints(entry.getValue());
            if (skill != null && !selectedSkills.contains(skill)) {
                result.add(skill);
            }
        }
        extraSkills = result.size();
        return result;
    }
}