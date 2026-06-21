package fuass.logic;

import fuass.model.Armor;
import fuass.model.Skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmorComboState {

    private static final int BAD_SKILL_THRESHOLD = -10;

    // ---skill tracking---

    private final List<Skill> targetSkills;
    private final Map<String, Integer> skillRegistry;
    private final int[] pointsTracker;
    private final int[] platePointsTracker;
    private int torsoIncCounter = 0;

    // ---slots tracking---

    // indices 0-4 → armor positions, index 5 → weapon
    private final int[] slots;
    private int totalSlots;
    private int plateSlots;
    private final int weaponSlots;

    public ArmorComboState(List<Skill> targetSkills, Map<String, Integer> skillRegistry,
                           int totalUniqueSkills, int weaponSlots) {
        this.targetSkills = targetSkills;
        this.skillRegistry = skillRegistry;
        this.pointsTracker = new int[totalUniqueSkills];
        this.platePointsTracker = new int[totalUniqueSkills];
        this.slots = new int[6]; // 5 armor positions + weapon
        this.weaponSlots = weaponSlots;
        this.slots[5] = weaponSlots;
        this.totalSlots = weaponSlots;
    }

    public void addArmor(Armor armor, int pos) {
        if (armor == null) return;

        int s = armor.getSlots();
        slots[pos] = s;
        totalSlots += s;

        if (armor.isPlate()) {
            plateSlots = s;
            addPlatePoints(armor.getSkills());
        } else if (armor.hasTorsoInc()) {
            torsoIncCounter++;
            applyTorsoIncBonus();
        } else {
            addPoints(armor.getSkills());
        }
    }

    public void removeArmor(Armor armor, int pos) {
        if (armor == null) return;

        totalSlots -= slots[pos];
        slots[pos] = 0;

        if (armor.isPlate()) {
            plateSlots = 0;
            removePlatePoints(armor.getSkills());
        } else if (armor.hasTorsoInc()) {
            torsoIncCounter--;
            undoTorsoIncBonus();
        } else {
            removePoints(armor.getSkills());
        }
    }

    public boolean areTargetSkillsActivated() {
        for (Skill skill : targetSkills) {
            Integer idx = skillRegistry.get(skill.getPointsName());
            int current = (idx != null) ? pointsTracker[idx] : 0;
            int required = skill.getPointsToActivate();

            if (required > 0 && current < required) return false;
            if (required < 0 && current > required) return false;
        }
        return true;
    }

    public boolean hasAnyBadSkill() {
        for (int points : pointsTracker) {
            if (points <= BAD_SKILL_THRESHOLD) return true;
        }
        return false;
    }

    public Map<String, Integer> getCurrentTargetPoints() {
        Map<String, Integer> result = new HashMap<>(targetSkills.size() * 2);
        for (Skill skill : targetSkills) {
            String name = skill.getPointsName();
            Integer idx = skillRegistry.get(name);
            result.put(name, idx != null ? pointsTracker[idx] : 0);
        }
        return result;
    }

    public Map<String, Integer> getMissingTargetPoints() {
        Map<String, Integer> result = new HashMap<>();
        for (Skill skill : targetSkills) {
            String name = skill.getPointsName();
            int required = skill.getPointsToActivate();
            Integer idx = skillRegistry.get(name);
            int current = (idx != null) ? pointsTracker[idx] : 0;
            if ((required > 0 && current < required) || (required < 0 && current > required)) {
                result.put(name, required - current);
            }
        }
        return result;
    }

    // ---getters---

    public int[] getSlotsCopy()  { return slots.clone(); }
    public int getTotalSlots()   { return totalSlots; }
    public int getPlateSlots()   { return plateSlots; }
    public int getTorsoCounter() { return torsoIncCounter; }
    public int getWeaponSlots()  { return weaponSlots; }

    // ---helpers---

    private void addPoints(Map<String, Integer> pointsMap) {
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            Integer idx = skillRegistry.get(entry.getKey());
            if (idx != null) pointsTracker[idx] += entry.getValue();
        }
    }

    private void removePoints(Map<String, Integer> pointsMap) {
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            Integer idx = skillRegistry.get(entry.getKey());
            if (idx != null) pointsTracker[idx] -= entry.getValue();
        }
    }

    private void addPlatePoints(Map<String, Integer> pointsMap) {
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            Integer idx = skillRegistry.get(entry.getKey());
            if (idx != null) {
                int v = entry.getValue();
                platePointsTracker[idx] += v;
                pointsTracker[idx] += v;
            }
        }
    }

    private void removePlatePoints(Map<String, Integer> pointsMap) {
        for (Map.Entry<String, Integer> entry : pointsMap.entrySet()) {
            Integer idx = skillRegistry.get(entry.getKey());
            if (idx != null) {
                int v = entry.getValue();
                platePointsTracker[idx] -= v;
                pointsTracker[idx] -= v;
            }
        }
    }

    private void applyTorsoIncBonus() {
        for (int i = 0; i < pointsTracker.length; i++) {
            pointsTracker[i] += platePointsTracker[i];
        }
    }

    private void undoTorsoIncBonus() {
        for (int i = 0; i < pointsTracker.length; i++) {
            pointsTracker[i] -= platePointsTracker[i];
        }
    }
}