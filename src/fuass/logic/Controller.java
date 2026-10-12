package fuass.logic;

import fuass.io.DataStore;
import fuass.model.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Coordinates the search workflow: filters candidates, applies Pareto pruning,
 * and delegates combination finding to the {@link Combinator}.
 */
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

    private List<Skill> addTorsoIncToSkills(List<Skill> selectedSkills, FilterOptions filterOptions) {
        List<Skill> skillsPlusTorso = new ArrayList<>(selectedSkills);
        if (filterOptions.isAllowTorsoInc()) {
            skillsPlusTorso.add(new Skill("Torso Inc"));
        }
        return skillsPlusTorso;
    }

    public DataPool getFilteredCandidates(List<Skill> selectedSkills, FilterOptions filterOptions) {

        List<Skill> skillsPlusTorso = addTorsoIncToSkills(selectedSkills, filterOptions);

        // ---build predicates---

        Predicate<Armor> armorFilter = predicateMaker.armorPredicate(skillsPlusTorso, filterOptions);
        Predicate<Decoration> decorationFilter = predicateMaker.decorationPredicate(selectedSkills, filterOptions);

        // ---filter raw data---

        List<Armor> helmetsFiltered          = filterList(dataStore.getHelmets(),     armorFilter);
        List<Armor> platesFiltered           = filterList(dataStore.getPlates(),      armorFilter);
        List<Armor> gauntletsFiltered        = filterList(dataStore.getGauntlets(),   armorFilter);
        List<Armor> waistsFiltered           = filterList(dataStore.getWaists(),      armorFilter);
        List<Armor> leggingsFiltered         = filterList(dataStore.getLeggings(),    armorFilter);
        List<Decoration> decorationsFiltered = filterList(dataStore.getDecorations(), decorationFilter);

        return new DataPool(helmetsFiltered, platesFiltered, gauntletsFiltered, waistsFiltered, leggingsFiltered,
                decorationsFiltered);
    }

    public DataPool getParetoPool(DataPool pool,
                                           List<Skill> selectedSkills, FilterOptions filterOptions) {

        // ---dominance (pareto) pruning---

        List<Skill> skillsPlusTorso = addTorsoIncToSkills(selectedSkills, filterOptions);

        List<Armor> bestHelmets          = DominanceFilter.armorParetoFilter(pool.helmets(),    skillsPlusTorso);
        List<Armor> bestPlates           = DominanceFilter.armorParetoFilter(pool.plates(),     selectedSkills);
        List<Armor> bestGauntlets        = DominanceFilter.armorParetoFilter(pool.gauntlets(),  skillsPlusTorso);
        List<Armor> bestWaists           = DominanceFilter.armorParetoFilter(pool.waists(),     skillsPlusTorso);
        List<Armor> bestLeggings         = DominanceFilter.armorParetoFilter(pool.leggings(),   skillsPlusTorso);
        List<Decoration> bestDecorations = DominanceFilter.decoParetoFilter(pool.decorations(), selectedSkills);

        return new DataPool(bestHelmets, bestPlates, bestGauntlets, bestWaists, bestLeggings, bestDecorations);
    }

    public List<Combination> combine(DataPool pool, List<Skill> selectedSkills, FilterOptions filterOptions)
            throws InterruptedException {

        List<Armor> bestHelmets = new ArrayList<>(pool.helmets());
        List<Armor> bestPlates = new ArrayList<>(pool.plates());
        List<Armor> bestGauntlets = new ArrayList<>(pool.gauntlets());
        List<Armor> bestWaists = new ArrayList<>(pool.waists());
        List<Armor> bestLeggings = new ArrayList<>(pool.leggings());
        List<Decoration> bestDecorations = new ArrayList<>(pool.decorations());

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

    public List<Combination> search(List<Skill> selectedSkills, FilterOptions filterOptions)
            throws InterruptedException {

        DataPool pool = getFilteredCandidates(selectedSkills, filterOptions);
        pool = getParetoPool(pool, selectedSkills, filterOptions);
        return combine(pool, selectedSkills, filterOptions);
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

    // TODO: add wildcard armors ("any helmet with X slots") as fallback candidates
    /*
    addAnyArmorWithXSlots(helmetsFiltered,   "helmet");
    addAnyArmorWithXSlots(platesFiltered,    "plate");
    addAnyArmorWithXSlots(gauntletsFiltered, "gauntlet");
    addAnyArmorWithXSlots(waistsFiltered,    "waist");
    addAnyArmorWithXSlots(leggingsFiltered,  "legging");
    */

    /*
    private void addAnyArmorWithXSlots(List<Armor> list, String type) {
        list.add(new Armor(type, 1));
        list.add(new Armor(type, 2));
        list.add(new Armor(type, 3));
    }*/
}