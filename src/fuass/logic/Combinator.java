package fuass.logic;

import fuass.model.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Combinator {

    private final Consumer<Integer> progressCallback;
    private final SwingWorker<?, ?> worker;

    private double progressFactor;
    private final AtomicInteger lastReportedProgress = new AtomicInteger(0);
    private Map<Integer, Double> efficiencyCache;

    public Combinator(Consumer<Integer> progressCallback, SwingWorker<?, ?> worker) {
        this.progressCallback = progressCallback;
        this.worker = worker;
    }

    public List<Combination> combineWithPruning(List<List<Armor>> allArmors, List<Decoration> decorations,
                                                List<Skill> skills, FilterOptions filterOptions)
            throws InterruptedException {

        long start = System.nanoTime();

        double totalCombinations = 1;
        for (int i = 0; i < 5; i++) {
            totalCombinations *= allArmors.get(i).size();
        }
        // progress is reported from 0 to 90; the caller is responsible for the final 10
        progressFactor = 90.0 / totalCombinations;
        lastReportedProgress.set(0);

        // pre-compute decoration efficiency scores, keyed by id, to avoid recomputation during sort
        efficiencyCache = new HashMap<>();
        for (Decoration d : decorations) {
            efficiencyCache.put(d.getId(), d.getEfficiency(skills));
        }

        decorations.sort(
                Comparator.comparingInt(Decoration::getSize).reversed()
                        .thenComparing(d -> efficiencyCache.get(d.getId()), Comparator.reverseOrder())
        );

        final boolean allowBadSkills = filterOptions.isAllowBadSkills();

        // build a global skill index so armor and decoration skills can be tracked
        // as flat int arrays instead of HashMaps during backtracking
        Map<String, Integer> globalSkillRegistry = new HashMap<>();
        int nextIndex = 0;

        for (List<Armor> armorList : allArmors) {
            for (Armor armor : armorList) {
                if (armor == null) continue;
                for (String skillName : armor.getSkills().keySet()) {
                    if (!globalSkillRegistry.containsKey(skillName)) {
                        globalSkillRegistry.put(skillName, nextIndex++);
                    }
                }
            }
        }
        for (Decoration deco : decorations) {
            for (String skillName : deco.getSkills().keySet()) {
                if (!globalSkillRegistry.containsKey(skillName)) {
                    globalSkillRegistry.put(skillName, nextIndex++);
                }
            }
        }
        int totalUniqueSkills = nextIndex;

        ArmorComboState state = new ArmorComboState(skills, globalSkillRegistry, totalUniqueSkills,
                filterOptions.getWeaponSlots());

        List<Combination> validCombinations = new ArrayList<>();
        AtomicInteger currentCount = new AtomicInteger(0);

        combineRecursive(allArmors, decorations, skills, new ArrayList<>(5), validCombinations,
                allowBadSkills, 0, true, currentCount, state);

        progressCallback.accept(90);

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Search completed in: " + elapsed + " ms");

        return validCombinations;
    }

    // ---progress bar---

    private void progressCalculator(AtomicInteger currentCount) {
        int current = currentCount.incrementAndGet();
        int progress = Math.min(90, (int) (current * progressFactor));

        if (progress > lastReportedProgress.get()) {
            lastReportedProgress.set(progress);
            progressCallback.accept(progress);
        }
    }

    // ---armor backtracking---

    private void combineRecursive(List<List<Armor>> allArmors,
                                  List<Decoration> decorations,
                                  List<Skill> skills,
                                  List<Armor> currentArmorCombo,
                                  List<Combination> validCombinations,
                                  boolean allowBadSkills,
                                  int pos,
                                  boolean plateIsEmpty,
                                  AtomicInteger currentCount,
                                  ArmorComboState state) throws InterruptedException {

        if (worker.isCancelled()) throw new InterruptedException();

        if (state.areTargetSkillsActivated()) {
            if (!allowBadSkills && state.hasAnyBadSkill()) return;

            ArrayList<Armor> finalCombination = new ArrayList<>(5);
            finalCombination.addAll(currentArmorCombo);
            for (int i = finalCombination.size(); i < 5; i++) finalCombination.add(null);
            validCombinations.add(new Combination(finalCombination, Collections.emptyList(), null,
                    state.getWeaponSlots(), -1));
            return;
        }

        if (tryWithDecos(decorations, currentArmorCombo, skills, validCombinations, allowBadSkills, state)) {
            return;
        }

        if (pos == allArmors.size()) return;

        for (Armor armor : allArmors.get(pos)) {

            // a Torso Inc piece multiplies the plate's skills, so it is useless without a plate
            if (plateIsEmpty && armor != null && armor.hasTorsoInc()) continue;

            currentArmorCombo.add(armor);
            state.addArmor(armor, pos);

            // pos 0 is the plate; once placed, Torso Inc pieces become eligible
            boolean newPlateIsEmpty = (pos != 0 || armor == null) && plateIsEmpty;

            combineRecursive(allArmors, decorations, skills, currentArmorCombo, validCombinations,
                    allowBadSkills, pos + 1, newPlateIsEmpty, currentCount, state);

            currentArmorCombo.removeLast();
            state.removeArmor(armor, pos);
            progressCalculator(currentCount);
        }
    }

    // ---decoration backtracking---

    private void combineDecosRecursive(int index, List<Decoration> decorations,
                                       List<Integer> currentCombo,
                                       int currentSlots, int targetSlots,
                                       int[] slots,
                                       boolean[] found,
                                       List<Decoration>[] solution,
                                       int[] currentPts,
                                       int[] requiredPts,
                                       int[][] decoContribs) {

        if (found[0]) return;

        if (currentSlots == targetSlots) {
            if (areSkillsActivatedFast(currentPts, requiredPts)) {
                found[0] = true;
                List<Decoration> result = new ArrayList<>(currentCombo.size());
                for (int idx : currentCombo) result.add(decorations.get(idx));
                solution[0] = result;
            }
            return;
        }

        if (currentSlots > targetSlots) return;

        for (int i = index; i < decorations.size(); i++) {
            int decoSize = decorations.get(i).getSize();
            int slotUsedIndex = tryPlaceDecoEfficient(decoSize, slots);
            if (slotUsedIndex == -1) continue;

            currentCombo.add(i);
            for (int j = 0; j < requiredPts.length; j++) currentPts[j] += decoContribs[i][j];

            combineDecosRecursive(i, decorations, currentCombo, currentSlots + decoSize,
                    targetSlots, slots, found, solution, currentPts, requiredPts, decoContribs);

            currentCombo.removeLast();
            for (int j = 0; j < requiredPts.length; j++) currentPts[j] -= decoContribs[i][j];
            undoPlaceDecoEfficient(decoSize, slotUsedIndex, slots);
        }
    }

    private boolean areSkillsActivatedFast(int[] current, int[] required) {
        for (int i = 0; i < required.length; i++) {
            if (required[i] > 0 && current[i] < required[i]) return false;
            if (required[i] < 0 && current[i] > required[i]) return false;
        }
        return true;
    }

    private int tryPlaceDecoEfficient(int decoSize, int[] slots) {
        int bestLargerIndex = -1;
        int bestLargerValue = Integer.MAX_VALUE;

        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            if (slot == decoSize) {
                slots[i] = 0;
                return i;
            } else if (slot > decoSize && slot < bestLargerValue) {
                bestLargerValue = slot;
                bestLargerIndex = i;
            }
        }

        if (bestLargerIndex != -1) {
            slots[bestLargerIndex] -= decoSize;
            return bestLargerIndex;
        }

        return -1;
    }

    private void undoPlaceDecoEfficient(int decoSize, int indexUsed, int[] slots) {
        if (indexUsed < 0) return;
        slots[indexUsed] += decoSize;
    }

    // ---decoration backtracking orchestration---

    private boolean tryWithDecos(List<Decoration> decorations,
                                 List<Armor> currentArmorCombo,
                                 List<Skill> skills,
                                 List<Combination> validCombinations,
                                 boolean allowBadSkills,
                                 ArmorComboState state) {

        if (decorations.isEmpty()) return false;

        int totalSlots = state.getTotalSlots();
        if (totalSlots == 0) return false;

        int[] slots = state.getSlotsCopy();

        List<Decoration> validDecos = Calculator.validDecorations(decorations, slots);
        if (validDecos.isEmpty()) return false;

        Map<String, Integer> currentPoints = state.getCurrentTargetPoints();
        Map<String, Integer> missingPoints = state.getMissingTargetPoints();
        int torsoCounter = state.getTorsoCounter();
        int plateSlots   = state.getPlateSlots();

        List<Decoration> usefulDecos;
        if (Calculator.hasBadMixJewels(skills, validDecos)) {
            usefulDecos = new ArrayList<>(validDecos);
        } else {
            usefulDecos = Calculator.usefulDecorations(validDecos, missingPoints);
        }

        double totalMissingPoints = Calculator.totalMissingPoints(missingPoints);
        final double idealEfficiency = Calculator.calculateBestEfficiency(usefulDecos, efficiencyCache);

        int optimistSlotsNeeded;
        if (torsoCounter > 0 && plateSlots > 0) {
            // Torso Inc case
            int torsoMultiplier = 1 + torsoCounter;
            double remainingPoints = totalMissingPoints;
            int usedPlateSlots = 0;

            for (int i = 0; i < plateSlots; i++) {
                remainingPoints -= idealEfficiency * torsoMultiplier;
                usedPlateSlots++;
                if (remainingPoints <= 0) break;
            }

            int otherSlotsNeeded = (remainingPoints > 0)
                    ? (int) Math.ceil(remainingPoints / idealEfficiency)
                    : 0;

            optimistSlotsNeeded = usedPlateSlots + otherSlotsNeeded;

        } else if (Calculator.hasGoodMixJewels(skills, validDecos)) {
            optimistSlotsNeeded = (int) Math.ceil(totalMissingPoints / idealEfficiency);

        } else {
            optimistSlotsNeeded = Calculator.minimumSlotsNeeded(missingPoints, usefulDecos);
        }

        if (optimistSlotsNeeded > totalSlots) return false;

        if (torsoCounter > 0 && plateSlots > 0) {
            return tryWithDecosTorsoCase(plateSlots, torsoCounter, usefulDecos, currentPoints,
                    optimistSlotsNeeded, totalSlots, slots, skills, allowBadSkills, currentArmorCombo,
                    state.getWeaponSlots(), validCombinations);

        } else {
            // standard case: no Torso Inc, or no slots in plate
            int S = skills.size();
            int D = usefulDecos.size();

            // pre-compute contribution matrix to avoid repeated map lookups in the recursive call
            int[][] decoContribs = new int[D][S];
            for (int i = 0; i < D; i++) {
                Decoration deco = usefulDecos.get(i);
                for (int j = 0; j < S; j++) {
                    decoContribs[i][j] = deco.getSkillPoints(skills.get(j).getPointsName());
                }
            }

            int[] currentPts = new int[S];
            int[] requiredPts = new int[S];
            for (int j = 0; j < S; j++) {
                String name = skills.get(j).getPointsName();
                currentPts[j]  = currentPoints.getOrDefault(name, 0);
                requiredPts[j] = skills.get(j).getPointsToActivate();
            }

            // single-element arrays used as mutable out-parameters (Java has no ref/out keyword)
            boolean[] found = {false};
            @SuppressWarnings("unchecked")
            List<Decoration>[] decoSolutionRef = new List[1];

            for (int usedSlots = optimistSlotsNeeded; usedSlots <= totalSlots; usedSlots++) {
                combineDecosRecursive(0, usefulDecos, new ArrayList<>(), 0, usedSlots, slots,
                        found, decoSolutionRef, currentPts, requiredPts, decoContribs);
                if (found[0]) break;
            }

            if (found[0]) {
                if (!allowBadSkills) {
                    if (Calculator.hasAnyBadSkill(
                            Calculator.getPointsMap(currentArmorCombo, decoSolutionRef[0], List.of(), torsoCounter))) {
                        return true;
                    }
                }
                List<Armor> okCombination = new ArrayList<>(currentArmorCombo);
                while (okCombination.size() < 5) okCombination.add(null);
                validCombinations.add(new Combination(okCombination, decoSolutionRef[0], null,
                        state.getWeaponSlots(), torsoCounter));
                return true;
            }
        }

        return false;
    }

    // --Torso Inc special case---

    private boolean tryWithDecosTorsoCase(int plateSlots, int torsoCounter, List<Decoration> decorations,
                                          Map<String, Integer> currentPoints, int optimistSlotsNeeded,
                                          int totalSlots, int[] slots, List<Skill> skills,
                                          boolean allowBadSkills, List<Armor> currentArmorCombo,
                                          int weaponSlots, List<Combination> validCombinations) {

        int[] plateSlotArr = {plateSlots};
        List<Decoration> plateValidDecos = Calculator.validDecorations(decorations, plateSlotArr);

        List<List<Decoration>> plateCombos = new ArrayList<>();
        generatePlateCombos(plateValidDecos, plateSlots, plateCombos, new ArrayList<>(), 0);

        int S = skills.size();
        int D = decorations.size();

        int[][] decoContribs = new int[D][S];
        for (int i = 0; i < D; i++) {
            Decoration deco = decorations.get(i);
            for (int j = 0; j < S; j++) {
                decoContribs[i][j] = deco.getSkillPoints(skills.get(j).getPointsName());
            }
        }

        int[] requiredPts = new int[S];
        for (int j = 0; j < S; j++) requiredPts[j] = skills.get(j).getPointsToActivate();

        int[] slotsWithoutPlate = slots.clone();
        slotsWithoutPlate[0] = 0;
        int slotsNeeded = optimistSlotsNeeded - plateSlots;
        int otherSlots  = totalSlots - plateSlots;

        List<Decoration> bestDecoCombo  = null;
        List<Decoration> bestPlateCombo = null;
        int bestSlotsUsed = Integer.MAX_VALUE;

        for (List<Decoration> plateCombo : plateCombos) {
            Map<String, Integer> newPoints = Calculator.applyTorsoPlatePoints(currentPoints, plateCombo, torsoCounter);

            int[] currentPts = new int[S];
            for (int j = 0; j < S; j++) {
                currentPts[j] = newPoints.getOrDefault(skills.get(j).getPointsName(), 0);
            }

            // single-element arrays used as mutable out-parameters (Java has no ref/out keyword)
            boolean[] found = {false};
            @SuppressWarnings("unchecked")
            List<Decoration>[] decoSolutionRef = new List[1];

            for (int usedSlots = slotsNeeded; usedSlots <= otherSlots; usedSlots++) {
                combineDecosRecursive(0, decorations, new ArrayList<>(), 0, usedSlots, slotsWithoutPlate,
                        found, decoSolutionRef, currentPts, requiredPts, decoContribs);

                if (found[0]) {
                    if (usedSlots < bestSlotsUsed) {
                        bestSlotsUsed  = usedSlots;
                        bestDecoCombo  = new ArrayList<>(decoSolutionRef[0]);
                        bestPlateCombo = plateCombo;
                    }
                    break;
                }
            }
        }

        if (bestDecoCombo != null) {
            if (!allowBadSkills) {
                if (Calculator.hasAnyBadSkill(
                        Calculator.getPointsMap(currentArmorCombo, bestDecoCombo, bestPlateCombo, torsoCounter))) {
                    return true;
                }
            }
            List<Armor> okCombination = new ArrayList<>(currentArmorCombo);
            while (okCombination.size() < 5) okCombination.add(null);
            validCombinations.add(new Combination(okCombination, bestDecoCombo, bestPlateCombo,
                    weaponSlots, torsoCounter));
            return true;
        }

        return false;
    }

    private void generatePlateCombos(List<Decoration> decorations, int plateSlotSize,
                                     List<List<Decoration>> results, List<Decoration> current, int startIndex) {
        results.add(new ArrayList<>(current));
        for (int i = startIndex; i < decorations.size(); i++) {
            Decoration deco = decorations.get(i);
            if (deco.getSize() <= plateSlotSize) {
                current.add(deco);
                generatePlateCombos(decorations, plateSlotSize - deco.getSize(), results, current, i);
                current.removeLast();
            }
        }
    }
}