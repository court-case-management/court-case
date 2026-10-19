package model;

/**
 * Party entity representing litigants.
 */
public class Party {
    private int partyId;
    private String partyName;
    private String contactNo;

    public Party() {}

    public Party(int partyId, String partyName, String contactNo) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.contactNo = contactNo;
    }

    public int getPartyId() { return partyId; }
    public void setPartyId(int partyId) { this.partyId = partyId; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    @Override
    public String toString() {
        return partyId + " - " + partyName;
    }
}
