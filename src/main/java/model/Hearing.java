package model;

import java.sql.Date;

/**
 * Hearing entity representing scheduled court hearings.
 */
public class Hearing {
    private int hearingId;
    private String caseNo;
    private int judgeId;
    private Date hearingDate;
    private String remarks;
    private String judgeName; // Helper field for display

    public Hearing() {}

    public Hearing(int hearingId, String caseNo, int judgeId, Date hearingDate, String remarks) {
        this.hearingId = hearingId;
        this.caseNo = caseNo;
        this.judgeId = judgeId;
        this.hearingDate = hearingDate;
        this.remarks = remarks;
    }

    public int getHearingId() { return hearingId; }
    public void setHearingId(int hearingId) { this.hearingId = hearingId; }

    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String caseNo) { this.caseNo = caseNo; }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public Date getHearingDate() { return hearingDate; }
    public void setHearingDate(Date hearingDate) { this.hearingDate = hearingDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }
}
