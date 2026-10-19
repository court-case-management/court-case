package model;

import java.sql.Date;

/**
 * CaseModel entity representing court case details.
 */
public class CaseModel {
    private String caseNo;
    private String caseTitle;
    private Date filingDate;
    private String caseType;
    private String status;
    private int courtId;
    private String courtName; // Helper field for display

    public CaseModel() {}

    public CaseModel(String caseNo, String caseTitle, Date filingDate, String caseType, String status, int courtId) {
        this.caseNo = caseNo;
        this.caseTitle = caseTitle;
        this.filingDate = filingDate;
        this.caseType = caseType;
        this.status = status;
        this.courtId = courtId;
    }

    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String caseNo) { this.caseNo = caseNo; }

    public String getCaseTitle() { return caseTitle; }
    public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }

    public Date getFilingDate() { return filingDate; }
    public void setFilingDate(Date filingDate) { this.filingDate = filingDate; }

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCourtId() { return courtId; }
    public void setCourtId(int courtId) { this.courtId = courtId; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    @Override
    public String toString() {
        return caseNo + " - " + caseTitle;
    }
}
