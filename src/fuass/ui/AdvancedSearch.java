package fuass.ui;

import fuass.model.Armor;
import fuass.model.Decoration;
import fuass.model.DataPool;
import fuass.model.FilterOptions;
import fuass.model.Skill;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * Modal dialog for advanced search, allowing users to manually select
 * specific armors and decorations from the filtered pool.
 */
public class AdvancedSearch extends JDialog {

    private static final int SCROLL_UNIT_INCREMENT = 10;

    // --- selection cache ---
    private record CacheKey(FilterOptions filters, List<Skill> skills) {}
    private record CacheValue(
            Set<String> checkedArmors,
            Set<String> checkedDecorations,
            Map<String, Set<Integer>> checkedWildcardsByColumn
    ) {}

    private static final Map<CacheKey, CacheValue> SELECTION_CACHE = new HashMap<>();

    private JPanel mainPanel;
    private JScrollPane helmetsScrollPanel;
    private JScrollPane platesScrollPanel;
    private JScrollPane gauntletsScrollPanel;
    private JScrollPane waistsScrollPanel;
    private JScrollPane leggingsScrollPanel;
    private JScrollPane decorationsScrollPanel;
    private JButton searchButton;
    private JButton cancelButton;
    private JButton helmetsDefaultButton;
    private JButton helmetsAllButton;
    private JButton helmetsNoneButton;
    private JButton platesDefaultButton;
    private JButton platesAllButton;
    private JButton platesNoneButton;
    private JButton gauntletsDefaultButton;
    private JButton gauntletsAllButton;
    private JButton gauntletsNoneButton;
    private JButton waistsDefaultButton;
    private JButton waistsAllButton;
    private JButton waistsNoneButton;
    private JButton leggingsDefaultButton;
    private JButton leggingsAllButton;
    private JButton leggingsNoneButton;
    private JButton decorationsDefaultButton;
    private JButton decorationsAllButton;
    private JButton decorationsNoneButton;

    private List<JCheckBox> helmetsBoxes;
    private List<JCheckBox> platesBoxes;
    private List<JCheckBox> gauntletsBoxes;
    private List<JCheckBox> waistsBoxes;
    private List<JCheckBox> leggingsBoxes;
    private List<JCheckBox> decorationsBoxes;

    private List<JCheckBox> helmetsWildcardBoxes;
    private List<JCheckBox> platesWildcardBoxes;
    private List<JCheckBox> gauntletsWildcardBoxes;
    private List<JCheckBox> waistsWildcardBoxes;
    private List<JCheckBox> leggingsWildcardBoxes;

    private final DataPool pool;
    private final DataPool paretoPool;
    private final FilterOptions filterOptions;
    private final List<Skill> selectedSkills;

    private DataPool result;

    // --- constructor ---

    public AdvancedSearch(Frame parent, DataPool pool, DataPool paretoPool,
                          FilterOptions filterOptions, List<Skill> selectedSkills) {
        super(parent, "Advanced Search", true);
        this.pool = pool;
        this.paretoPool = paretoPool;
        this.filterOptions = filterOptions;
        this.selectedSkills = selectedSkills;

        setContentPane(mainPanel);
        populateColumns();
        restoreCachedSelection();
        initButtons();

        pack();
        if (parent != null) {
            setSize(getWidth(), parent.getHeight());
        }
        setLocationRelativeTo(parent);

        // blocks here until dispose() is called, since the dialog is modal
        setVisible(true);
    }

    // --- result ---

    public DataPool getResult() {
        return result;
    }

    // --- selection cache ---

    private void restoreCachedSelection() {
        CacheKey key = new CacheKey(filterOptions, selectedSkills);
        CacheValue cached = SELECTION_CACHE.get(key);
        if (cached == null) return;

        restoreBoxes(helmetsBoxes, cached.checkedArmors());
        restoreBoxes(platesBoxes, cached.checkedArmors());
        restoreBoxes(gauntletsBoxes, cached.checkedArmors());
        restoreBoxes(waistsBoxes, cached.checkedArmors());
        restoreBoxes(leggingsBoxes, cached.checkedArmors());
        restoreBoxes(decorationsBoxes, cached.checkedDecorations());
        restoreWildcardBoxes(helmetsWildcardBoxes, cached.checkedWildcardsByColumn().get("helmet"));
        restoreWildcardBoxes(platesWildcardBoxes, cached.checkedWildcardsByColumn().get("plate"));
        restoreWildcardBoxes(gauntletsWildcardBoxes, cached.checkedWildcardsByColumn().get("gauntlet"));
        restoreWildcardBoxes(waistsWildcardBoxes, cached.checkedWildcardsByColumn().get("waist"));
        restoreWildcardBoxes(leggingsWildcardBoxes, cached.checkedWildcardsByColumn().get("legging"));
    }

    private void restoreBoxes(List<JCheckBox> boxes, Set<String> names) {
        for (int i = 0; i < boxes.size(); i++) {
            if (names.contains(boxes.get(i).getText())) {
                boxes.get(i).setSelected(true);
            }
        }
    }

    private void restoreWildcardBoxes(List<JCheckBox> wildcardBoxes, Set<Integer> checkedSlots) {
        if (checkedSlots == null) return;
        for (int slots = 1; slots <= wildcardBoxes.size(); slots++) {
            if (checkedSlots.contains(slots)) {
                wildcardBoxes.get(slots - 1).setSelected(true);
            }
        }
    }

    private void saveSelectionToCache() {
        CacheKey key = new CacheKey(filterOptions, selectedSkills);
        SELECTION_CACHE.put(key, new CacheValue(
                collectCheckedNames(helmetsBoxes, platesBoxes, gauntletsBoxes, waistsBoxes, leggingsBoxes),
                collectCheckedNames(decorationsBoxes),
                collectCheckedWildcardsByColumn()
        ));
    }

    private Map<String, Set<Integer>> collectCheckedWildcardsByColumn() {
        Map<String, Set<Integer>> wildcards = new HashMap<>();
        wildcards.put("helmet", collectCheckedWildcards(helmetsWildcardBoxes));
        wildcards.put("plate", collectCheckedWildcards(platesWildcardBoxes));
        wildcards.put("gauntlet", collectCheckedWildcards(gauntletsWildcardBoxes));
        wildcards.put("waist", collectCheckedWildcards(waistsWildcardBoxes));
        wildcards.put("legging", collectCheckedWildcards(leggingsWildcardBoxes));
        return wildcards;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private final Set<String> collectCheckedNames(List<JCheckBox>... boxLists) {
        Set<String> names = new HashSet<>();
        for (List<JCheckBox> boxes : boxLists) {
            for (JCheckBox box : boxes) {
                if (box.isSelected()) {
                    names.add(box.getText());
                }
            }
        }
        return names;
    }

    private Set<Integer> collectCheckedWildcards(List<JCheckBox> wildcardBoxes) {
        Set<Integer> slots = new HashSet<>();
        for (int i = 0; i < wildcardBoxes.size(); i++) {
            if (wildcardBoxes.get(i).isSelected()) {
                slots.add(i + 1);
            }
        }
        return slots;
    }

    // --- column population ---

    private record ColumnBoxes(List<JCheckBox> boxes, List<JCheckBox> wildcardBoxes) {}

    private void populateColumns() {

        ColumnBoxes helmets = fillColumn(helmetsScrollPanel, pool.helmets(), paretoPool.helmets(),
                Armor::getName, "helmet");
        helmetsBoxes = helmets.boxes();
        helmetsWildcardBoxes = helmets.wildcardBoxes();

        ColumnBoxes plates = fillColumn(platesScrollPanel, pool.plates(), paretoPool.plates(),
                Armor::getName, "plate");
        platesBoxes = plates.boxes();
        platesWildcardBoxes = plates.wildcardBoxes();

        ColumnBoxes gauntlets = fillColumn(gauntletsScrollPanel, pool.gauntlets(), paretoPool.gauntlets(),
                Armor::getName, "gauntlet");
        gauntletsBoxes = gauntlets.boxes();
        gauntletsWildcardBoxes = gauntlets.wildcardBoxes();

        ColumnBoxes waists = fillColumn(waistsScrollPanel, pool.waists(), paretoPool.waists(),
                Armor::getName, "waist");
        waistsBoxes = waists.boxes();
        waistsWildcardBoxes = waists.wildcardBoxes();

        ColumnBoxes leggings = fillColumn(leggingsScrollPanel, pool.leggings(), paretoPool.leggings(),
                Armor::getName, "legging");
        leggingsBoxes = leggings.boxes();
        leggingsWildcardBoxes = leggings.wildcardBoxes();

        // decorations have no wildcard concept, so wildcardType is null:
        // no separator/wildcard boxes are created for this column
        ColumnBoxes decorations = fillColumn(decorationsScrollPanel, pool.decorations(),
                paretoPool.decorations(), Decoration::getName, null);
        decorationsBoxes = decorations.boxes();
    }

    private <T> ColumnBoxes fillColumn(JScrollPane scrollPane, List<T> items, List<T> paretoItems,
                                       Function<T, String> nameExtractor,
                                       String wildcardType) {

        JPanel checklistArea = new JPanel();
        checklistArea.setLayout(new BoxLayout(checklistArea, BoxLayout.Y_AXIS));
        checklistArea.setBackground(Color.WHITE);

        // wildcards always start unchecked (JCheckBox default)
        List<JCheckBox> wildcardBoxes = new ArrayList<>();
        if (wildcardType != null) {
            for (int slots = 1; slots <= 3; slots++) {
                // reuse Armor's own name formatting instead of building the label by hand
                String label = new Armor(wildcardType, slots).getName();

                JCheckBox wildcardBox = new JCheckBox(label);
                wildcardBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                wildcardBox.setBackground(Color.WHITE);
                checklistArea.add(wildcardBox);
                wildcardBoxes.add(wildcardBox);
            }

            JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            checklistArea.add(separator);
        }

        // "isDefault" means pareto membership: it drives the bold font, the initial
        // checked state and what the Default button restores.
        // Identity-based on purpose: paretoPool must hold the same instances as pool,
        // otherwise nothing would match (no bold, nothing checked by default).
        Set<T> paretoSet = Collections.newSetFromMap(new IdentityHashMap<>());
        paretoSet.addAll(paretoItems);

        List<JCheckBox> boxes = new ArrayList<>();
        for (T item : items) {
            boolean isDefault = paretoSet.contains(item);

            // same detail popup as the result rows, shown on right-click only
            PopupCheckbox checkBox = new PopupCheckbox(nameExtractor.apply(item),
                    () -> buildPopupFor(item));
            checkBox.setSelected(isDefault);
            checkBox.putClientProperty("isDefault", isDefault);
            checkBox.setDefaultStyle(isDefault);
            checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            checkBox.setBackground(Color.WHITE);
            checklistArea.add(checkBox);
            boxes.add(checkBox);
        }

        scrollPane.setViewportView(checklistArea);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
        return new ColumnBoxes(boxes, wildcardBoxes);
    }

    private JPopupMenu buildPopupFor(Object item) {
        if (item instanceof Armor armor) {
            return ItemPopupFactory.forArmor(armor);
        }
        if (item instanceof Decoration deco) {
            return ItemPopupFactory.forDecoration(deco);
        }
        return new JPopupMenu();
    }

    // --- buttons ---

    private void initButtons() {
        searchButton.addActionListener(_ -> {
            saveSelectionToCache();
            result = buildResultFromCheckboxes();
            dispose();
        });

        cancelButton.addActionListener(_ -> {
            result = null;
            dispose();
        });

        wireColumn(helmetsDefaultButton,     helmetsAllButton,     helmetsNoneButton,     helmetsBoxes,     helmetsWildcardBoxes);
        wireColumn(platesDefaultButton,      platesAllButton,      platesNoneButton,      platesBoxes,      platesWildcardBoxes);
        wireColumn(gauntletsDefaultButton,   gauntletsAllButton,   gauntletsNoneButton,   gauntletsBoxes,   gauntletsWildcardBoxes);
        wireColumn(waistsDefaultButton,      waistsAllButton,      waistsNoneButton,      waistsBoxes,      waistsWildcardBoxes);
        wireColumn(leggingsDefaultButton,    leggingsAllButton,    leggingsNoneButton,    leggingsBoxes,    leggingsWildcardBoxes);
        wireColumn(decorationsDefaultButton, decorationsAllButton, decorationsNoneButton, decorationsBoxes, List.of());
    }

    // Default/All/None act on both the regular pool checkboxes and the wildcard
    // checkboxes of the same column (decorations simply have no wildcards)
    private void wireColumn(JButton defaultButton, JButton allButton, JButton noneButton,
                            List<JCheckBox> boxes, List<JCheckBox> wildcardBoxes) {
        List<JCheckBox> all = combined(boxes, wildcardBoxes);
        wireDefaultButton(defaultButton, all);
        wireAllNoneButton(allButton, all, true);
        wireAllNoneButton(noneButton, all, false);
    }

    private List<JCheckBox> combined(List<JCheckBox> boxes, List<JCheckBox> wildcardBoxes) {
        List<JCheckBox> all = new ArrayList<>(boxes);
        all.addAll(wildcardBoxes);
        return all;
    }

    private void wireDefaultButton(JButton button, List<JCheckBox> boxes) {
        button.addActionListener(_ -> {
            for (JCheckBox box : boxes) {
                boolean isDefault = Boolean.TRUE.equals(box.getClientProperty("isDefault"));
                box.setSelected(isDefault);
            }
        });
    }

    private void wireAllNoneButton(JButton button, List<JCheckBox> boxes, boolean selected) {
        button.addActionListener(_ -> setAllSelected(boxes, selected));
    }

    private void setAllSelected(List<JCheckBox> boxes, boolean selected) {
        for (JCheckBox box : boxes) {
            box.setSelected(selected);
        }
    }

    // --- result building ---

    private DataPool buildResultFromCheckboxes() {
        return new DataPool(
                onlyCheckedWithWildcards(pool.helmets(),   helmetsBoxes,   helmetsWildcardBoxes,   "helmet"),
                onlyCheckedWithWildcards(pool.plates(),    platesBoxes,    platesWildcardBoxes,    "plate"),
                onlyCheckedWithWildcards(pool.gauntlets(), gauntletsBoxes, gauntletsWildcardBoxes, "gauntlet"),
                onlyCheckedWithWildcards(pool.waists(),    waistsBoxes,    waistsWildcardBoxes,    "waist"),
                onlyCheckedWithWildcards(pool.leggings(),  leggingsBoxes,  leggingsWildcardBoxes,  "legging"),
                onlyChecked(pool.decorations(), decorationsBoxes)
        );
    }

    private List<Armor> onlyCheckedWithWildcards(List<Armor> items, List<JCheckBox> boxes,
                                                 List<JCheckBox> wildcardBoxes, String type) {
        List<Armor> checked = onlyChecked(items, boxes);
        for (int slots = 1; slots <= wildcardBoxes.size(); slots++) {
            if (wildcardBoxes.get(slots - 1).isSelected()) {
                checked.add(new Armor(type, slots));
            }
        }
        return checked;
    }

    // Relies on boxes being in the same order as items (both come from the same
    // list in fillColumn), so boxes.get(i) is always the checkbox of items.get(i)
    private <T> List<T> onlyChecked(List<T> items, List<JCheckBox> boxes) {
        List<T> checked = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (boxes.get(i).isSelected()) {
                checked.add(items.get(i));
            }
        }
        return checked;
    }
}