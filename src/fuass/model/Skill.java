package fuass.model;

public class Skill {

    private final int id;
    private final String name;
    private final int pointsToActivate;
    private final String pointsName;
    private boolean isVisible;

    public Skill(String[] data) {
        id = Integer.parseInt(data[0]);
        name = data[1];
        pointsToActivate = Integer.parseInt(data[2]);
        pointsName = data[3];
        isVisible = true;
    }

    // for Torso Inc
    public Skill(String name) {
        this.name = name;
        pointsName = name;
        id = 0;
        pointsToActivate = 0;
        isVisible = false;
    }

    // ---getters---

    public int getId()                { return id; }
    public String getName()           { return name; }
    public int getPointsToActivate()  { return pointsToActivate; }
    public String getPointsName()     { return pointsName; }
    public boolean isVisible()        { return isVisible; }

    // ---setters---

    public void setVisible(boolean visible) { isVisible = visible; }
}