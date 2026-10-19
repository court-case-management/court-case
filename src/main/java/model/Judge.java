package model;

/**
 * Judge entity representing judge information.
 */
public class Judge {
    private int judgeId;
    private String judgeName;
    private String designation;
    private int courtId;

    public Judge() {}

    public Judge(int judgeId, String judgeName, String designation, int courtId) {
        this.judgeId = judgeId;
        this.judgeName = judgeName;
        this.designation = designation;
        this.courtId = courtId;
    }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    @Override
    public String toString() {
        return judgeId + " - " + judgeName + " (" + designation + ")";
    }
}
