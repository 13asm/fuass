package fuass.ui;

import fuass.io.DataStore;
import fuass.logic.Controller;
import fuass.model.Combination;
import fuass.model.FilterOptions;
import fuass.model.Skill;
import fuass.model.SkillGroup;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class GUI extends JFrame {

    // ---form components---

    private JPanel mainPanel;
    private JScrollPane mainPanelLeft;
    private JPanel mainPanelRight;
    private JComboBox<String> sortResultsComboBox;
    private JButton orderButton;
    private JScrollPane resultsScrollPanel;
    private JPanel resultsPanel;
    private JPanel searchButtonsPanel;
    private JPanel progressBarPanel;
    private JProgressBar progressBar;
    private JComboBox<String> genderComboBox;
    private JComboBox<String> hunterRankComboBox;
    private JComboBox<String> villageRankComboBox;
    private JComboBox<String> weaponClassComboBox;
    private JComboBox<String> weaponSlotsComboBox;
    private JCheckBox allowBadSkillsCheckBox;
    private JCheckBox allowTorsoIncCheckBox;
    private JCheckBox allowDummyCheckBox;
    private JComboBox<String> skillsComboBox;
    private JPanel skillsPanel;
    private JButton searchButton;
    private JButton advancedSearchButton;
    private JButton stopButton;

    // ---data---

    private final DataStore dataStore;

    // ---skill state---

    private List<Skill> allSkills;
    private Map<String, SkillGroup> allSkillGroups;
    private final List<Skill> selectedSkills = new ArrayList<>();
    private List<Skill> previousSelectedSkills;

    // ---search state---

    private SwingWorker<Void, Integer> searchWorker;
    private List<Combination> searchResults;
    private final ResultsBuilder resultsBuilder;
    private boolean sortDescending;

    // ---constructor---

    public GUI(DataStore dataStore) {
        super("fuass v0.1.0");
        this.dataStore = dataStore;
        initWindow();
        initComponents();
        resultsBuilder = new ResultsBuilder();
    }

    // ---window and component init---

    private void initWindow() {
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        initHunterOptions();
        initWeaponOptions();
        initSkillsManager();
        initResultsPanel();
        initResultsOptions();
        initOrderButton();
        initAllowBadSkillsCheckBox();
        allowDummyCheckBox.setSelected(true);
        initStopButton();
        setupSearchButton();
    }

    private void populateComboBox(JComboBox<String> comboBox, String[] items) {
        for (String item : items) {
            comboBox.addItem(item);
            MetalLookAndFeel metal = new MetalLookAndFeel();
            UIDefaults metalDefaults = metal.getDefaults();
            InputMap metalInputMap = (InputMap) metalDefaults.get("ComboBox.ancestorInputMap");
            comboBox.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, metalInputMap);
        }
    }

    private void initHunterOptions() {
        populateComboBox(genderComboBox, new String[]{"Male", "Female"});
        populateComboBox(villageRankComboBox, new String[]{
                "1☆", "2☆", "3☆", "4☆", "5☆", "6☆",
                "7☆ (Nekoht)", "8☆ (Nekoht)", "9☆ (Nekoht)"
        });
        villageRankComboBox.setSelectedIndex(8);
        populateComboBox(hunterRankComboBox, new String[]{
                "HR1 (LR)", "HR2 (LR)", "HR3 (LR)",
                "HR4 (HR)", "HR5 (HR)", "HR6 (HR)",
                "HR7 (GR)", "HR8 (GR)", "HR9 (GR)"
        });
        hunterRankComboBox.setSelectedIndex(8);
    }

    private void initWeaponOptions() {
        populateComboBox(weaponClassComboBox, new String[]{"Blademaster", "Gunner"});
        populateComboBox(weaponSlotsComboBox, new String[]{"- - -", "o - -", "o o -", "o o o"});
    }

    // ---skill manager---

    private void initSkillsManager() {
        mainPanelLeft.getVerticalScrollBar().setUnitIncrement(10);
        mainPanelLeft.getHorizontalScrollBar().setUnitIncrement(10);
        skillsPanel.setLayout(new BoxLayout(skillsPanel, BoxLayout.Y_AXIS));

        loadAllSkillsData();
        loadAllSkillGroups(allSkills);
        refreshSkillsComboBox();

        skillsComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                String selectedSkillName = (String) skillsComboBox.getSelectedItem();
                if (selectedSkillName == null) return;
                Skill skill = findSkillByName(selectedSkillName);
                if (skill == null) return;
                selectedSkills.add(skill);
                addSkillTag(skill);
                setSkillVisibility(skill, false);
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }

    private void initAllowBadSkillsCheckBox() {
        allowBadSkillsCheckBox.setSelected(true);
        allowBadSkillsCheckBox.addActionListener(_ -> {
            if (!allowBadSkillsCheckBox.isSelected()) {
                removeBadSkillsTags();
            }
            refreshSkillsComboBox();
        });
    }

    private void loadAllSkillsData() {
        allSkills = new ArrayList<>(dataStore.getSkills());
    }

    private void loadAllSkillGroups(List<Skill> skills) {
        allSkillGroups = new HashMap<>();
        for (Skill skill : skills) {
            String groupName = skill.getPointsName();
            SkillGroup group = allSkillGroups.computeIfAbsent(groupName, SkillGroup::new);
            group.addSkill(skill);
        }
    }

    private Skill findSkillByName(String skillName) {
        for (Skill skill : allSkills) {
            if (skill.getName().equals(skillName)) return skill;
        }
        return null;
    }

    private void setSkillVisibility(Skill selectedSkill, boolean isVisible) {
        for (Skill skill : allSkills) {
            if (skill.getPointsName().equals(selectedSkill.getPointsName())) {
                skill.setVisible(isVisible);
            }
        }
        refreshSkillsComboBox();
    }

    private void addSkillTag(Skill skill) {
        JPanel tag = new JPanel(new BorderLayout());
        tag.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        JLabel label = new JLabel(skill.getName());
        JButton closeButton = new JButton(" x ");
        closeButton.setBorder(BorderFactory.createEmptyBorder());

        closeButton.addActionListener(_ -> {
            selectedSkills.remove(skill);
            skillsPanel.remove(tag);
            skillsPanel.revalidate();
            skillsPanel.repaint();
            setSkillVisibility(skill, true);
        });

        tag.add(label, BorderLayout.CENTER);
        tag.add(closeButton, BorderLayout.EAST);

        Dimension pref = tag.getPreferredSize();
        tag.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        skillsPanel.add(tag);
        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    private void removeBadSkillsTags() {
        Component[] components = skillsPanel.getComponents();
        List<Component> toRemove = new ArrayList<>();

        for (Component c : components) {
            if (c instanceof JPanel tagPanel) {
                Component first = tagPanel.getComponent(0);
                if (!(first instanceof JLabel label)) continue;
                String skillName = label.getText();
                Skill skill = findSkillByName(skillName);
                if (skill != null && skill.getPointsToActivate() < 0) {
                    toRemove.add(c);
                    selectedSkills.remove(skill);
                    setSkillVisibility(skill, true);
                }
            }
        }

        for (Component tag : toRemove) {
            skillsPanel.remove(tag);
        }
        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    public void refreshSkillsComboBox() {
        skillsComboBox.removeAllItems();
        for (Skill skill : allSkills) {
            if (skill.isVisible()) {
                if (skill.getPointsToActivate() < 0 && !allowBadSkillsCheckBox.isSelected()) continue;
                skillsComboBox.addItem(skill.getName());
            }
        }
        skillsComboBox.setSelectedIndex(-1);
    }

    // ---results---

    private void initResultsPanel() {
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsScrollPanel.getVerticalScrollBar().setUnitIncrement(10);
        resultsScrollPanel.getHorizontalScrollBar().setUnitIncrement(10);
    }

    private void initResultsOptions() {
        populateComboBox(sortResultsComboBox, new String[]{
                "Free Slots", "Base Defense", "Max Defense", "Fire Res", "Water Res",
                "Thunder Res", "Ice Res", "Dragon Res", "Extra Skills"
        });
        sortResultsComboBox.setSelectedIndex(-1);
        sortResultsComboBox.addActionListener(_ -> {
            String selectedSort = (String) sortResultsComboBox.getSelectedItem();
            sortAndDisplayResults(selectedSort);
        });
    }

    private void initOrderButton() {
        orderButton.addActionListener(_ -> {
            sortDescending = !sortDescending;
            orderButton.setText(sortDescending ? "↓" : "↑");
            String selectedSort = (String) sortResultsComboBox.getSelectedItem();
            sortAndDisplayResults(selectedSort);
        });
    }

    private void sortAndDisplayResults(String sortOption) {
        if (searchResults == null || sortOption == null) return;

        List<Combination> sortedList = new ArrayList<>(searchResults);

        Comparator<Combination> baseComparator = switch (sortOption) {
            case "Free Slots"   -> Comparator.comparingInt(Combination::getFreeSlots);
            case "Base Defense" -> Comparator.comparingInt(Combination::getDefense);
            case "Max Defense"  -> Comparator.comparingInt(Combination::getMaxDefense);
            case "Fire Res"     -> Comparator.comparingInt(Combination::getFireRes);
            case "Water Res"    -> Comparator.comparingInt(Combination::getWaterRes);
            case "Thunder Res"  -> Comparator.comparingInt(Combination::getThunderRes);
            case "Ice Res"      -> Comparator.comparingInt(Combination::getIceRes);
            case "Dragon Res"   -> Comparator.comparingInt(Combination::getDragonRes);
            case "Extra Skills" -> Comparator.comparingInt(Combination::getExtraSkills);
            default             -> null;
        };

        if (baseComparator != null) {
            Comparator<Combination> fullComparator = baseComparator
                    .thenComparingInt(Combination::getFreeSlots)
                    .thenComparingInt(Combination::getMaxDefense);
            if (sortDescending) fullComparator = fullComparator.reversed();
            sortedList.sort(fullComparator);
        }

        JPanel panel = resultsBuilder.buildResultsPanel(sortedList, allSkillGroups, previousSelectedSkills);
        resultsPanel.removeAll();
        resultsPanel.add(panel);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ---search---

    private void initStopButton() {
        stopButton.addActionListener(_ -> {
            if (searchWorker != null && !searchWorker.isDone()) {
                searchWorker.cancel(true);
            }
        });
    }

    private void setupSearchButton() {
        searchButton.addActionListener(_ -> startSearchInBackground());
    }

    private FilterOptions gatherFilterOptions() {
        return new FilterOptions(
                (String) genderComboBox.getSelectedItem(),
                (String) hunterRankComboBox.getSelectedItem(),
                (String) villageRankComboBox.getSelectedItem(),
                (String) weaponClassComboBox.getSelectedItem(),
                (String) weaponSlotsComboBox.getSelectedItem(),
                allowBadSkillsCheckBox.isSelected(),
                allowTorsoIncCheckBox.isSelected(),
                allowDummyCheckBox.isSelected()
        );
    }

    private void startSearchInBackground() {
        if (selectedSkills == null || selectedSkills.isEmpty()) return;

        final FilterOptions capturedOptions = gatherFilterOptions();
        previousSelectedSkills = new ArrayList<>(selectedSkills);
        final List<Skill> skillsSnapshot = new ArrayList<>(selectedSkills);

        searchWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws InterruptedException {
                searchResults = null;

                SwingUtilities.invokeLater(() -> {
                    resultsPanel.removeAll();
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                    progressBar.setValue(0);
                });

                Consumer<Integer> progressUpdater = this::publish;
                Controller controller = new Controller(progressUpdater, this, dataStore);
                searchResults = controller.search(skillsSnapshot, capturedOptions);

                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.getLast());
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        onSearchCancelled();
                        return;
                    }
                    get(); // rethrows any exception thrown in doInBackground
                    onSearchComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        searchWorker.execute();
    }

    private void onSearchCancelled() {
        searchResults = null;
        resultsPanel.removeAll();
        resultsPanel.add(new JLabel("Search stopped"));
        resultsPanel.revalidate();
        resultsPanel.repaint();
        progressBar.setValue(0);
    }

    private void onSearchComplete() {
        if (searchResults != null) {
            for (int i = 0; i < searchResults.size(); i++) {
                searchResults.get(i).setId(i + 1);
            }
        }
        sortDescending = true;
        sortAndDisplayResults("Free Slots");
        sortResultsComboBox.setSelectedIndex(0);
        orderButton.setText("↓");
        progressBar.setValue(100);
    }
}