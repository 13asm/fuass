package fuass.logic;

import fuass.io.DataStore;
import fuass.model.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Controller {

    private final Consumer<Integer> progressCallback;
    private final SwingWorker<?, ?> worker;
    private final DataStore dataStore;
    private final PredicateMaker predicateMaker = new PredicateMaker();

    public Controller(Consumer<Integer> progressCallback, SwingWorker<?, ?> worker, DataStore dataStore) {
        this.progressCallback = progressCallback;
        this.worker = worker;
        this.dataStore = dataStore;
    }

    public List<Combination> search(List<Skill> selectedSkills, FilterOptions filterOptions)
            throws InterruptedException {

        List<Skill> skillsWithTorso = new ArrayList<>(selectedSkills);
        if (filterOptions.isAllowTorsoInc()) {
            skillsWithTorso.add(new Skill("Torso Inc"));
        }

        // ---build predicates---

        Predicate<Armor> armorFilter = predicateMaker.armorPredicate(skillsWithTorso, filterOptions);
        Predicate<Decoration> decorationFilter = predicateMaker.decorationPredicate(selectedSkills, filterOptions);

        // ---filter raw data---

        List<Armor> helmetsFiltered          = filterList(dataStore.getHelmets(),     armorFilter);
        List<Armor> platesFiltered           = filterList(dataStore.getPlates(),      armorFilter);
        List<Armor> gauntletsFiltered        = filterList(dataStore.getGauntlets(),   armorFilter);
        List<Armor> waistsFiltered           = filterList(dataStore.getWaists(),      armorFilter);
        List<Armor> leggingsFiltered         = filterList(dataStore.getLeggings(),    armorFilter);
        List<Decoration> decorationsFiltered = filterList(dataStore.getDecorations(), decorationFilter);

        // TODO: add wildcard armors ("any helmet with X slots") as fallback candidates
        /*
        addAnyArmorWithXSlots(helmetsFiltered,   "helmet");
        addAnyArmorWithXSlots(platesFiltered,    "plate");
        addAnyArmorWithXSlots(gauntletsFiltered, "gauntlet");
        addAnyArmorWithXSlots(waistsFiltered,    "waist");
        addAnyArmorWithXSlots(leggingsFiltered,  "legging");
        */

        // ---dominance (pareto) pruning---

        List<Armor> bestHelmets          = DominanceFilter.armorParetoFilter(helmetsFiltered,   skillsWithTorso);
        List<Armor> bestPlates           = DominanceFilter.armorParetoFilter(platesFiltered,    selectedSkills);
        List<Armor> bestGauntlets        = DominanceFilter.armorParetoFilter(gauntletsFiltered, skillsWithTorso);
        List<Armor> bestWaists           = DominanceFilter.armorParetoFilter(waistsFiltered,    skillsWithTorso);
        List<Armor> bestLeggings         = DominanceFilter.armorParetoFilter(leggingsFiltered,  skillsWithTorso);
        List<Decoration> bestDecorations = DominanceFilter.decoParetoFilter(decorationsFiltered, selectedSkills);

        // null represents an empty armor slot
        // empty-slot pieces are added to allow partial solutions
        bestHelmets.add(null);
        bestPlates.add(null);
        bestGauntlets.add(null);
        bestWaists.add(null);
        bestLeggings.add(null);

        // plate is placed at index 0
        // so the combinator processes it first for Torso Inc efficiency
        List<List<Armor>> allArmors = new ArrayList<>();
        Collections.addAll(allArmors, bestPlates, bestHelmets, bestGauntlets, bestWaists, bestLeggings);

        // ---combine with pruning---

        Combinator combinator = new Combinator(progressCallback, worker);
        return combinator.combineWithPruning(allArmors, bestDecorations, selectedSkills, filterOptions);
    }

    private <T> List<T> filterList(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
    /*
    private void addAnyArmorWithXSlots(List<Armor> list, String type) {
        list.add(new Armor(type, 1));
        list.add(new Armor(type, 2));
        list.add(new Armor(type, 3));
    }*/
}