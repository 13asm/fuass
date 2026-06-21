package fuass.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterOptions {

    private static final Pattern DIGITS_PATTERN = Pattern.compile("\\d+");

    private final String gender;
    private final int hunterRank;
    private final int villageRank;
    private final String weaponClass;
    private final int weaponSlots;
    private final boolean allowBadSkills;
    private final boolean allowTorsoInc;
    private final boolean allowDummy;

    public FilterOptions(String gender, String hunterRank, String villageRank,
                         String weaponClass, String weaponSlots,
                         boolean allowBadSkills, boolean allowTorsoInc, boolean allowDummy) {

        this.gender = gender;
        this.hunterRank = parseRank(hunterRank);
        this.villageRank = parseRank(villageRank);
        this.weaponClass = weaponClass;
        this.weaponSlots = parseWeaponSlots(weaponSlots);
        this.allowBadSkills = allowBadSkills;
        this.allowTorsoInc = allowTorsoInc;
        this.allowDummy = allowDummy;
    }

    // ---getters---

    public String getGender()         { return gender; }
    public int getHunterRank()        { return hunterRank; }
    public int getVillageRank()       { return villageRank; }
    public String getWeaponClass()    { return weaponClass; }
    public int getWeaponSlots()       { return weaponSlots; }
    public boolean isAllowBadSkills() { return allowBadSkills; }
    public boolean isAllowTorsoInc()  { return allowTorsoInc; }
    public boolean isAllowDummy()     { return allowDummy; }

    // ---methods---

    private int parseRank(String rankLabel) {
        Matcher matcher = DIGITS_PATTERN.matcher(rankLabel);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 0;
    }

    private int parseWeaponSlots(String slotPattern) {
        return switch (slotPattern) {
            case "o - -" -> 1;
            case "o o -" -> 2;
            case "o o o" -> 3;
            default -> 0; // covers "- - -"
        };
    }
}
