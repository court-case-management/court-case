package model;

/**
 * Court entity representing judicial court details.
 */
public class Court {
    private int courtId;
    private String courtName;
    private String courtLevel;
    private String location;

    public Court() {}

    public Court(int courtId, String courtName, String courtLevel, String location) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.courtLevel = courtLevel;
        this.location = location;
    }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getCourtLevel() { return courtLevel; }
    public void setCourtLevel(String courtLevel) { this.courtLevel = courtLevel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return courtId + " - " + courtName + " (" + courtLevel + ")";
    }
}
