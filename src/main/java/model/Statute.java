package model;

/**
 * Statute entity representing legal acts and section numbers.
 */
public class Statute {
    private int statuteId;
    private String actName;
    private String sectionNo;
    private String description;

    public Statute() {}

    public Statute(int statuteId, String actName, String sectionNo, String description) {
        this.statuteId = statuteId;
        this.actName = actName;
        this.sectionNo = sectionNo;
        this.description = description;
    }

    public int getStatuteId() { return statuteId; }
    public void setStatuteId(int statuteId) { this.statuteId = statuteId; }

    public String getActName() { return actName; }
    public void setActName(String actName) { this.actName = actName; }

    public String getSectionNo() { return sectionNo; }
    public void setSectionNo(String sectionNo) { this.sectionNo = sectionNo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return statuteId + " - " + actName + " (" + sectionNo + ")";
    }
}
