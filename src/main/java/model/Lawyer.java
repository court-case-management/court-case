package model;

/**
 * Lawyer entity representing advocate information.
 */
public class Lawyer {
    private int lawyerId;
    private String lawyerName;
    private String barRegNo;
    private String specialization;

    public Lawyer() {}

    public Lawyer(int lawyerId, String lawyerName, String barRegNo, String specialization) {
        this.lawyerId = lawyerId;
        this.lawyerName = lawyerName;
        this.barRegNo = barRegNo;
        this.specialization = specialization;
    }

    public int getLawyerId() { return lawyerId; }
    public void setLawyerId(int lawyerId) { this.lawyerId = lawyerId; }

    public String getLawyerName() { return lawyerName; }
    public void setLawyerName(String lawyerName) { this.lawyerName = lawyerName; }

    public String getBarRegNo() { return barRegNo; }
    public void setBarRegNo(String barRegNo) { this.barRegNo = barRegNo; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    @Override
    public String toString() {
        return lawyerId + " - " + lawyerName + " (" + barRegNo + ")";
    }
}
