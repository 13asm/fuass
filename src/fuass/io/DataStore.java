package fuass.io;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.Skill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads and provides access to all game data (armors, decorations, skills)
 * from CSV files. Acts as the single source of truth for raw data.
 */
public class DataStore {

    private final String pathHelmets;
    private final String pathPlates;
    private final String pathGauntlets;
    private final String pathWaists;
    private final String pathLeggings;
    private final String pathDecorations;
    private final String pathSkills;

    private final List<Armor> helmets;
    private final List<Armor> plates;
    private final List<Armor> gauntlets;
    private final List<Armor> waists;
    private final List<Armor> leggings;
    private final List<Decoration> decorations;
    private final List<Skill> skills;

    public DataStore() throws IOException {
        this("data/helmets.csv", "data/plates.csv", "data/gauntlets.csv",
             "data/waists.csv", "data/leggings.csv", "data/decorations.csv",
             "data/skills.csv");
    }

    public DataStore(String pathHelmets, String pathPlates, String pathGauntlets,
                    String pathWaists, String pathLeggings, String pathDecorations,
                    String pathSkills) throws IOException {
        this.pathHelmets = pathHelmets;
        this.pathPlates = pathPlates;
        this.pathGauntlets = pathGauntlets;
        this.pathWaists = pathWaists;
        this.pathLeggings = pathLeggings;
        this.pathDecorations = pathDecorations;
        this.pathSkills = pathSkills;

        helmets = loadArmorList(pathHelmets, "helmet");
        plates = loadArmorList(pathPlates, "plate");
        gauntlets = loadArmorList(pathGauntlets, "gauntlet");
        waists = loadArmorList(pathWaists, "waist");
        leggings = loadArmorList(pathLeggings, "legging");
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
        for (String[] row : CSVReader.readRows(pathDecorations)) {
            result.add(new Decoration(row));
        }
        return result;
    }

    private List<Skill> loadSkillList() throws IOException {
        List<Skill> result = new ArrayList<>();
        for (String[] row : CSVReader.readRows(pathSkills)) {
            result.add(new Skill(row));
        }
        return result;
    }
}