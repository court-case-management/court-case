# Court Case Management System - Oracle DBMS Mini-Project

A complete, beginner-friendly desktop application built with **Java Swing**, **JDBC**, and **Oracle Database 21c (XE)**. Designed for college DBMS mini-projects and viva examinations.

---

## 📁 Project Folder Structure

```text
court-case-management/
├── pom.xml                     # Maven build configuration with ojdbc11 driver
├── README.md                   # Setup guide, demo walkthrough & viva preparation
├── sql/
│   ├── schema.sql              # Table definitions, constraints & sequences
│   ├── data.sql                # Realistic Indian legal sample data (20+ cases, 22 citations)
│   └── plsql.sql               # Stored procedures, functions, triggers & ref cursor
└── src/
    └── main/
        └── java/
            ├── Main.java       # Application entry point
            ├── db/
            │   └── DBConnection.java  # Oracle JDBC connection configuration
            ├── model/          # POJO Entity classes (Court, Judge, CaseModel, Lawyer, etc.)
            ├── dao/            # Data Access Objects using PreparedStatement & CallableStatement
            │   ├── CourtDAO.java
            │   ├── JudgeDAO.java
            │   ├── CaseDAO.java
            │   ├── LawyerDAO.java
            │   ├── PartyDAO.java
            │   ├── HearingDAO.java
            │   ├── StatuteDAO.java
            │   ├── CitationDAO.java
            │   ├── ReportDAO.java
            │   └── ProcedureDAO.java
            └── ui/             # Clean Swing GUI Panels for all 10 tabs
                ├── MainFrame.java
                ├── CasesPanel.java
                ├── CourtsPanel.java
                ├── JudgesPanel.java
                ├── LawyersPanel.java
                ├── PartiesPanel.java
                ├── HearingsPanel.java
                ├── CitationsPanel.java
                ├── ReportsPanel.java
                ├── SqlConsolePanel.java
                └── ProceduresPanel.java
```

---

## 🛠️ Tech Stack & Requirements

- **Java**: Java 17 or higher
- **Database**: Oracle Database 21c Express Edition (XE) or standard Oracle DB
- **Database Service Name**: `XEPDB1` (Default pluggable database for Oracle XE 21c)
- **Database Driver**: Oracle JDBC Driver (`ojdbc11:21.9.0.0`)
- **UI Framework**: Java Swing (Clean desktop interface using standard `JTabbedPane`, `JTable`, `JTextField`, `JButton`)

---

## ⚙️ Step 1: How to Install Oracle Database XE & Setup User

1. Download and install **Oracle Database Express Edition 21c (XE)** for Windows/Linux.
2. Open SQL*Plus or SQL Developer as SYSDBA:
   ```sql
   sqlplus / as sysdba
   ```
3. Switch to the pluggable database `XEPDB1`:
   ```sql
   ALTER SESSION SET CONTAINER = XEPDB1;
   ```
4. Create a database user account for the mini-project:
   ```sql
   CREATE USER court_admin IDENTIFIED BY oracle;
   GRANT CONNECT, RESOURCE, DBA TO court_admin;
   GRANT UNLIMITED TABLESPACE TO court_admin;
   ```

---

## 🗄️ Step 2: Run SQL Scripts in Order

Connect as `court_admin` to `XEPDB1` service (or run as `system` user):

```bash
# Command line execution via SQL*Plus
sqlplus system/oracle@localhost:1521/XEPDB1
```

Run the three SQL scripts in the following exact sequence:

```sql
-- 1. Create Tables and Constraints
@sql/schema.sql

-- 2. Insert Sample Data (Landmark Indian Cases & Citation Graph)
@sql/data.sql

-- 3. Create PL/SQL Procedures, Functions, Triggers & Cursors
@sql/plsql.sql
```

*(Or open the script files in **Oracle SQL Developer** / **DBeaver** and execute `schema.sql`, `data.sql`, `plsql.sql` in order).*

---

## 🔌 Step 3: Configure Database Connection (`DBConnection.java`)

Open `src/main/java/db/DBConnection.java` and adjust connection credentials if necessary:

```java
public class DBConnection {
    private static String dbUrl = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
    private static String dbUser = "system";       // or "court_admin"
    private static String dbPassword = "oracle";   // your Oracle DB password
    ...
}
```

---

## 🚀 Step 4: Compile & Run the Application

### Option A: Using Maven (Recommended)
```bash
# Compile project
mvn clean compile

# Run application
mvn exec:java -Dexec.mainClass="Main"
```

### Option B: Using standard `javac` command line
```bash
# Compile source files into bin directory
mkdir bin
javac -cp "lib/ojdbc11.jar" -d bin src/main/java/db/*.java src/main/java/model/*.java src/main/java/dao/*.java src/main/java/ui/*.java src/main/java/Main.java

# Run application
java -cp "bin;lib/ojdbc11.jar" Main
```

---

## 🎬 Step 5: 5-Step Viva Presentation Walkthrough

Follow these 5 simple steps during your viva demonstration to showcase all key features to your external examiner:

### Step 1: Browse & Search Court Cases (`JOIN`)
- Open the **Cases** tab.
- Notice all 20 landmark cases listed in the table along with their assigned Court Name (joined from `COURT` table).
- Type `"Puttaswamy"` in the Search Title box and click **Search** to demonstrate filtered query execution.

### Step 2: Add & Audit Case (`PL/SQL Procedure + Audit Trigger`)
- In the **Cases** tab, enter:
  - Case No: `SC-2024-999`
  - Title: `College DBMS Mini Project Case`
  - Filing Date: `2024-06-01`
  - Case Type: `Civil`
  - Status: `Open`
  - Court: `Supreme Court of India`
- Click **Insert Case (add_case)**. This calls the PL/SQL stored procedure `add_case`.
- Change Status dropdown to `Closed` and click **Update Status (update_case_status)**.
- Switch to the **SQL Console** tab and run `SELECT * FROM AUDIT_LOG ORDER BY Changed_On DESC`. Show the examiner how trigger `trg_status_audit` automatically logged the old and new status.

### Step 3: Validate Business Logic Triggers (`ORA-20001 & ORA-20002`)
- Go to the **Hearings** tab.
- Click **Test Trigger Error (Invalid Date Demo)**.
- The app attempts to schedule a hearing dated `1940-01-01` for a case filed in 2018.
- Show the popup error message: `ORA-20001: Hearing date cannot be before case filing date!`. This proves trigger `trg_hearing_date_check` is enforcing database integrity.
- Switch to **Citations Network** tab and click **Test Self-Citation Trigger**. Show popup `ORA-20002: Self-Citation Error: Case cannot cite itself!`.

### Step 4: Test PL/SQL Stored Functions & REF CURSOR
- On the **Citations Network** tab, select case `SC-1978-02` (Maneka Gandhi v. Union of India) and click **Get Citation Count (get_citation_count)**. Show the return value from stored function `get_citation_count`.
- Go to **Hearings** tab, select case `SC-2018-05` and click **View Hearings via SYS_REFCURSOR**. Show how JDBC receives the output cursor from stored procedure `get_case_hearings`.

### Step 5: Hierarchical Query & Live DDL Console (`CONNECT BY PRIOR`)
- Open **Queries & Reports** tab.
- Click **7. Citation Chain (CONNECT BY PRIOR)** with root case `SC-2018-05`.
- Demonstrate the graph traversal output showing multi-level legal precedent chains (e.g. *Puttaswamy -> Shreya Singhal -> Maneka Gandhi -> Kesavananda Bharati -> AK Gopalan*).
- Switch to **SQL Console** tab and click **Demo ALTER (Add Email to LAWYER)** to demonstrate live DDL command execution (`ALTER TABLE LAWYER ADD Email VARCHAR2(100)`).

---

## 🎓 Summary of DBMS & PL/SQL Concepts Demonstrated (Viva Reference)

| Concept Category | Features & Examples in Project |
| :--- | :--- |
| **Data Definition Language (DDL)** | `CREATE TABLE`, `ALTER TABLE`, Primary Keys, Unique Keys, Foreign Keys with `ON DELETE CASCADE` / `ON DELETE SET NULL`, `CHECK` constraints, `IDENTITY` columns. |
| **Data Manipulation Language (DML)** | `INSERT`, `UPDATE`, `DELETE`, `COMMIT`, transactional rollback handling. |
| **Relational Queries & Aggregations** | `INNER JOIN`, `LEFT JOIN`, `GROUP BY`, `HAVING` clause, `ORDER BY`, `COUNT()`, `NVL()`, `TO_DATE()`, `TO_CHAR()`. |
| **Subqueries** | Subqueries using `IN (SELECT Case_No FROM CASE_STATUTE ...)` |
| **Hierarchical Graph Queries** | Oracle-specific `CONNECT BY PRIOR` and `START WITH` clauses for precedent chain depth traversal. |
| **PL/SQL Stored Procedures** | `add_case`, `schedule_hearing`, `update_case_status`, `delete_case` with parameter binding and exception handling (`DUP_VAL_ON_INDEX`, `NO_DATA_FOUND`). |
| **PL/SQL Stored Functions** | `get_citation_count`, `get_hearing_count` returning scalar numbers. |
| **PL/SQL Cursors** | `get_case_hearings` procedure returning `SYS_REFCURSOR` parameter to Java JDBC. |
| **PL/SQL Triggers** | `trg_hearing_date_check` (Before Insert/Update validation), `trg_status_audit` (After Update auditing), `trg_no_self_citation` (Constraint check), custom error raising via `RAISE_APPLICATION_ERROR`. |
| **Java Database Connectivity (JDBC)** | Plain Java JDBC using `PreparedStatement`, `CallableStatement`, `ResultSetMetaData`, connection lifecycle, and `try-with-resources`. |
