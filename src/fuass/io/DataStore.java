package fuass.io;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.Skill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataStore {

    private static final String PATH_HELMETS = "data/helmets.csv";
    private static final String PATH_PLATES = "data/plates.csv";
    private static final String PATH_GAUNTLETS = "data/gauntlets.csv";
    private static final String PATH_WAISTS = "data/waists.csv";
    private static final String PATH_LEGGINGS = "data/leggings.csv";
    private static final String PATH_DECORATIONS = "data/decorations.csv";
    private static final String PATH_SKILLS = "data/skills.csv";

    private final List<Armor> helmets;
    private final List<Armor> plates;
    private final List<Armor> gauntlets;
    private final List<Armor> waists;
    private final List<Armor> leggings;
    private final List<Decoration> decorations;
    private final List<Skill> skills;


    public DataStore() throws IOException {
        helmets = loadArmorList(PATH_HELMETS, "helmet");
        plates = loadArmorList(PATH_PLATES, "plate");
        gauntlets = loadArmorList(PATH_GAUNTLETS, "gauntlet");
        waists = loadArmorList(PATH_WAISTS, "waist");
        leggings = loadArmorList(PATH_LEGGINGS, "legging");
        decorations = loadDecorationList();
        skills = loadSkillList();
    }

    // ---getters---

    public List<Armor> getHelmets()          { return Collections.unmodifiableList(helmets); }
    public List<Armor> getPlates()           { return Collections.unmodifiableList(plates); }
    public List<Armor> getGauntlets()        { return Collections.unmodifiableList(gauntlets); }
    public List<Armor> getWaists()           { return Collections.unmodifiableList(waists); }
    public List<Armor> getLeggings()         { return Collections.unmodifiableList(leggings); }
    public List<Decoration> getDecorations() { return Collections.unmodifiableList(decorations); }
    public List<Skill> getSkills()           { return Collections.unmodifiableList(skills); }

    // ---methods---

    private List<Armor> loadArmorList(String filePath, String type) throws IOException {
        List<Armor> armors = new ArrayList<>();
        for (String[] row : CSVReader.readRows(filePath)) {
            armors.add(new Armor(type, row));
        }
        return armors;
    }

    private List<Decoration> loadDecorationList() throws IOException {
        List<Decoration> result = new ArrayList<>();
        for (String[] row : CSVReader.readRows(DataStore.PATH_DECORATIONS)) {
            result.add(new Decoration(row));
        }
        return result;
    }

    private List<Skill> loadSkillList() throws IOException {
        List<Skill> result = new ArrayList<>();
        for (String[] row : CSVReader.readRows(DataStore.PATH_SKILLS)) {
            result.add(new Skill(row));
        }
        return result;
    }
}