package fuass.model;

import java.util.List;

public record DataPool(
        List<Armor> helmets,
        List<Armor> plates,
        List<Armor> gauntlets,
        List<Armor> waists,
        List<Armor> leggings,
        List<Decoration> decorations
){}