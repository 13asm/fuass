package fuass.logic;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.FilterOptions;
import fuass.model.Skill;

import java.util.List;
import java.util.function.Predicate;

public class PredicateMaker {

    public Predicate<Armor> armorPredicate(List<Skill> skills, FilterOptions filterOptions) {

        Predicate<Armor> combinedPredicate = _ -> false;
        for (Skill skill : skills) {
            Predicate<Armor> skillPointsPredicate;
            if (skill.getPointsToActivate() > 0) { // positive skill
                skillPointsPredicate = armor ->
                        armor.getSkillPoints(skill.getPointsName()) > 0;
            } else { // negative skill ("bad skill")
                skillPointsPredicate = armor ->
                        armor.getSkillPoints(skill.getPointsName()) < 0;
            }
            combinedPredicate = combinedPredicate.or(skillPointsPredicate);
        }

        // ---gender filter---

        Predicate<Armor> genderPredicate = armor ->
                armor.getGender().contains(filterOptions.getGender());
        combinedPredicate = combinedPredicate.and(genderPredicate);

        // ---weapon class---

        Predicate<Armor> weaponClassPredicate = armor ->
                armor.getHunterType().contains(filterOptions.getWeaponClass());
        combinedPredicate = combinedPredicate.and(weaponClassPredicate);

        if (!filterOptions.isAllowDummy()) {
            Predicate<Armor> dummyPredicate = armor ->
                    !armor.getName().contains("dummy");
            combinedPredicate = combinedPredicate.and(dummyPredicate);
        }

        // ---rank filter---

        Predicate<Armor> rankPredicate =
                ((Predicate<Armor>) armor -> armor.getVillageRank() <= filterOptions.getVillageRank())
                        .or(armor -> armor.getHunterRank() <= filterOptions.getHunterRank());
        combinedPredicate = combinedPredicate.and(rankPredicate);

        return combinedPredicate;
    }

    public Predicate<Decoration> decorationPredicate(List<Skill> skills, FilterOptions filterOptions) {

        Predicate<Decoration> combinedPredicate = _ -> false;
        for (Skill skill : skills) {
            Predicate<Decoration> skillPointsPredicate;
            if (skill.getPointsToActivate() > 0) { // positive skill
                skillPointsPredicate = decoration ->
                        decoration.getSkillPoints(skill.getPointsName()) > 0;
            } else { // negative skill ("bad skill")
                skillPointsPredicate = decoration ->
                        decoration.getSkillPoints(skill.getPointsName()) < 0;
            }
            combinedPredicate = combinedPredicate.or(skillPointsPredicate);
        }

        // ---rank filter---
        Predicate<Decoration> rankPredicate =
                ((Predicate<Decoration>) decoration -> decoration.getVillageRank() <= filterOptions.getVillageRank())
                        .or(decoration -> decoration.getHunterRank() <= filterOptions.getHunterRank());
        combinedPredicate = combinedPredicate.and(rankPredicate);

        return combinedPredicate;
    }
}