# Court Case Management System — Project Demonstration & Viva Script
**Course / Evaluation:** Mini-Project Phase 2 (DA2)  
**Database System:** Oracle Database 21c XE (`FREEPDB1`) | **Interface:** Java 26 Swing (JDBC `ojdbc11`)  
**Team Members:**
1. **Kanak Bansal** (`25BCE1109`)
2. **Sahana Karthikeyan** (`25BCE1295`)
3. **Avika Tyagi** (`25BCE1294`)

---

## 1. Equal Contribution Workflow Breakdown

| Team Member | Functional Domain & Responsibilities | Key Technical Deliverables |
| :--- | :--- | :--- |
| **Kanak Bansal**<br>`25BCE1109` | **Database Architecture, Relational Schema & Integrity Constraints** | • Two-tier system architecture design.<br>• Relational Schema normalized to BCNF (12 tables).<br>• DDL scripts: Identity primary keys, Check constraints, Foreign keys (`CASCADE` / `SET NULL`).<br>• Realistic Indian landmark case datasets & multi-level citation DAG ($\text{depth} \ge 4$). |
| **Sahana Karthikeyan**<br>`25BCE1295` | **PL/SQL Engine: Stored Routines, Cursors, Triggers & Exceptions** | • Stored Procedures: `add_case`, `schedule_hearing`, `update_case_status`, `delete_case`.<br>• Stored Functions: `get_citation_count`, `get_hearing_count`.<br>• Dynamic Ref Cursor: `get_case_hearings` (`SYS_REFCURSOR`).<br>• Active Triggers: `trg_hearing_date_check`, `trg_status_audit`, `trg_no_self_citation`.<br>• Custom exception trapping (`-20001` to `-20006`). |
| **Avika Tyagi**<br>`25BCE1294` | **Analytical SQL, Hierarchical Traversal, Java Swing GUI & Demo** | • Complex SQL (Q1–Q6): Multi-table JOINs, `GROUP BY`, `HAVING`, Subqueries, and Ternary resolution.<br>• Hierarchical recursive query: Q7 using `START WITH` and `CONNECT BY PRIOR`.<br>• Java Swing front-end architecture, JDBC driver integration, and SQL-injection defense.<br>• Live test execution, trigger error demonstration, and DA3 graph-database roadmap. |

---

## 2. Presentation & Demonstration Script

```
+-----------------------------------------------------------------------------------+
|                            PRESENTATION FLOW OVERVIEW                             |
|                                                                                   |
|  [ Kanak Bansal ]        -->    [ Sahana Karthikeyan ]    -->    [ Avika Tyagi ]  |
|  • Introduction                 • PL/SQL Business Logic          • Complex SQL    |
|  • Tech Stack & Architecture    • Stored Procedures              • Hierarchical   |
|  • 12-Table BCNF Schema         • Dynamic Ref Cursors              Traversal      |
|  • Relational Constraints       • Triggers & Auditing            • Java GUI &     |
|  • Citation DAG Dataset         • Custom Exception Trapping        Live Demo      |
|                                                                  • DA3 Roadmap    |
+-----------------------------------------------------------------------------------+
```

---

### **PART 1: Kanak Bansal (25BCE1109)**
**Domain:** System Architecture, BCNF Schema Normalization & Constraint Engineering

#### 1. Introduction & Problem Domain
* **Visual Cue:** Slide 1 / Report Title Page & Overview (Pages 1–3)
* **What to Say:**
  > *"Respected faculty, good morning. Today, our team—Sahana, Avika, and myself, Kanak—presents Phase 2 of our project: the **Court Case Management System with Graph-Based Precedent Analysis**.*
  >
  > *Judicial systems handle thousands of court cases, hearing schedules, statutes, and presiding judges every day. More fundamentally, under the common-law doctrine of precedent (*stare decisis*), decisions depend upon previous judicial rulings. Managing these complex citations manually or with unstructured records causes scheduling conflicts, data redundancies, and lost precedent chains.*
  >
  > *In DA1, we designed an ER model decomposed to Boyce-Codd Normal Form (BCNF). In this phase (DA2), our goal was the **physical database implementation in Oracle Database 21c XE**, ensuring all business rules, relational integrity, and normalization guarantees are enforced natively inside the database engine, with a Java Swing interface for demonstration."*

#### 2. Tiered System Architecture
* **Visual Cue:** Technology Stack Table (Page 5)
* **What to Say:**
  > *"The system uses a clean, two-tier architecture:
  > * **Database Engine:** Oracle Database 21c XE (service name: `FREEPDB1`). Oracle was chosen because all advanced procedural logic—such as stored procedures, triggers, custom exceptions, and cursors—is written in PL/SQL.
  > * **Application Layer:** Java 26 desktop client using Java Swing, providing a responsive tabbed interface without heavy third-party framework overhead.
  > * **Connectivity:** Oracle JDBC via the `ojdbc11` driver.
  >
  > A key design principle is that **all critical constraints and validation logic reside in the database**, not in the UI. Whether requests come from our Swing desktop application or a future web API, the same integrity rules are strictly enforced."*

#### 3. BCNF Relational Schema (12 Tables)
* **Visual Cue:** Table Creation DDL Scripts / SQL Developer (Pages 5–8, 9–10)
* **What to Say:**
  > *"Our normalized schema consists of **twelve relational tables**:
  > 
  > 1. **Core Entities:** `COURT`, `JUDGE`, `COURT_CASE`, `LAWYER`, `PARTY`, `HEARING`, and `STATUTE`. *(Note: `CASE` is a reserved word in Oracle SQL, so it was renamed to `COURT_CASE`)*.
  > 2. **Junction Entities:**
  >    * `CASE_PARTY`: Resolves the M:N relationship between cases and litigants.
  >    * `CASE_STATUTE`: Maps which laws and IPC sections apply to each case.
  >    * `REPRESENTS`: A ternary junction linking Lawyer, Party, and Case with composite primary key `(Lawyer_ID, Party_ID, Case_No)`. This allows an advocate to represent a specific party in a specific case without table duplication.
  > 3. **Citation Network:** The `CITATION` table models legal precedents connecting citing cases to cited cases. We enforced a table-level check: `CHECK (Citing_Case_No <> Cited_Case_No)` so a case can never cite itself.
  > 4. **Compliance & Auditing:** An independent `AUDIT_LOG` table with an identity primary key to record status changes. It intentionally has no foreign keys, ensuring the audit history remains permanent even if an old case record is archived."*

#### 4. Constraints & Integrity Design
* **Visual Cue:** DDL snippets with `CHECK`, `IDENTITY`, and `FOREIGN KEY` (Pages 6–8)
* **What to Say:**
  > *"We used Oracle's built-in constraint mechanisms to guarantee clean data:
  > * **Identity Columns:** Using `GENERATED BY DEFAULT AS IDENTITY` to generate surrogate keys automatically.
  > * **Domain Constraints:** `Court_Level` is restricted to `'District'`, `'High'`, or `'Supreme'`. `Status` is restricted to `'Open'`, `'Closed'`, or `'Under Appeal'`.
  > * **Referential Actions:** For judicial assignments, we used `ON DELETE SET NULL` so removing a court or judge doesn't erase historical case filings. For junction tables, we configured `ON DELETE CASCADE` so deleting a case cleanly removes its junction references."*

#### 5. Sample Dataset & Citation Graph Structure
* **Visual Cue:** Sample Data Insertion Scripts (Pages 11–19) / 'Cases' Tab in GUI (Page 41)
* **What to Say:**
  > *"To ensure realistic testing, we populated the database with 6 courts across District, High, and Supreme Court levels, 10 judges, 10 lawyers, 15 parties, 10 statutes, 25 hearings, and 20 landmark Indian judicial rulings—including *Kesavananda Bharati*, *Maneka Gandhi*, *Shreya Singhal*, and *Puttaswamy*.
  > 
  > Crucially, in the `CITATION` table, we created **multi-level citation chains with a depth of 4 levels**—for example, *Puttaswamy* citing *Shreya Singhal*, which cites *Maneka Gandhi*, which cites *Kesavananda Bharati*, which cites *A.K. Gopalan*. This complex chain directly challenges our query engine.
  > 
  > I will now hand over to **Sahana**, who will explain the server-side PL/SQL programming and business logic."*

---

### **PART 2: Sahana Karthikeyan (25BCE1295)**
**Domain:** PL/SQL Stored Procedures, Functions, Dynamic Cursors, Triggers & Exceptions

#### 1. Server-Side Business Logic Architecture
* **Visual Cue:** PL/SQL Programs Overview (Page 27) / 'Functions & Procedures' GUI Tab (Pages 46–47)
* **What to Say:**
  > *"Thank you, Kanak. While DDL provides the static blueprint, the operational integrity of a court management system depends entirely on its transaction rules. My contribution focused on writing and testing all **PL/SQL Stored Procedures, Functions, Cursors, Triggers, and Exception Handlers** directly within Oracle Database.
  > 
  > Centralizing business logic in PL/SQL reduces network roundtrips between the client application and database, and prevents duplicate validation code across different frontend forms."*

#### 2. Stored Procedures & Transaction Clean-up
* **Visual Cue:** PL/SQL Procedure code (Pages 27–30)
* **What to Say:**
  > *"I implemented four core procedures with explicit transaction boundaries:
  > 
  > * **`add_case`:** Atomically inserts a case into `COURT_CASE`. If an identical case number already exists, Oracle's `DUP_VAL_ON_INDEX` is trapped, and our custom exception `-20003` is raised with a clear message.
  > * **`schedule_hearing`:** Checks if the target case exists first (`COUNT(*) = 0`). If not, it halts before inserting and raises error `-20004`.
  > * **`update_case_status`:** Safely transitions a case status (e.g., from 'Open' to 'Under Appeal') using `SQL%NOTFOUND` verification. This procedure also fires our audit trigger.
  > * **`delete_case`:** Implements defensive deletion. Before dropping a case record, it cleans up all dependent child rows in the correct order: `REPRESENTS`, `CASE_PARTY`, `CASE_STATUTE`, `CITATION`, `HEARING`, and `AUDIT_LOG`, followed by an atomic `COMMIT`."*

#### 3. Stored Functions & Ref Cursors
* **Visual Cue:** Functions & Cursor Code (Pages 30–31, 34)
* **What to Say:**
  > *"Next, I created modular functions and a cursor procedure:
  > * **Functions `get_citation_count` and `get_hearing_count`:** These take a `Case_No`, run targeted aggregate lookups, handle `NO_DATA_FOUND` gracefully by returning `0`, and are directly callable from both SQL queries and our Java interface.
  > * **Cursor Procedure `get_case_hearings`:** Rather than returning static arrays, this uses an output parameter of type **`SYS_REFCURSOR`**. It queries the `HEARING` table joined with `JUDGE`, ordered by `Hearing_Date DESC`. This passes an active cursor pointer directly to our Java JDBC `CallableStatement`, allowing memory-efficient streaming of hearing lists."*

#### 4. Active Database Triggers
* **Visual Cue:** Triggers Code in Report (Pages 32–34)
* **What to Say:**
  > *"To ensure that illegal operations are blocked regardless of how the SQL is sent, I wrote three row-level triggers:
  > 
  > 1. **`trg_hearing_date_check` (`BEFORE INSERT OR UPDATE ON HEARING`):** A hearing cannot legally occur before a case is filed. The trigger intercepts `:NEW.Hearing_Date`, queries the parent case's `Filing_Date`, and if `:NEW.Hearing_Date < v_filing_date`, it raises application error **`-20001`**, preventing corrupt scheduling.
  > 2. **`trg_no_self_citation` (`BEFORE INSERT OR UPDATE ON CITATION`):** Validates `:NEW.Citing_Case_No <> :NEW.Cited_Case_No`. If violated, it raises error **`-20002`**.
  > 3. **`trg_status_audit` (`AFTER UPDATE OF Status ON COURT_CASE`):** Automatically triggers on case updates. If `OLD.Status <> NEW.Status`, it inserts a record containing the Case Number, old status, new status, and `SYSDATE` into `AUDIT_LOG`. This runs transparently without requiring any manual inserts from the user."*

#### 5. Exception Handling Matrix & Handover
* **Visual Cue:** Exception Handling Summary Table (Page 35–36)
* **What to Say:**
  > *"We standardized our user-defined error codes using Oracle's `RAISE_APPLICATION_ERROR`:
  > * `-20001`: Hearing date precedes filing date.
  > * `-20002`: Self-citation violation.
  > * `-20003`: Duplicate case primary key.
  > * `-20004`: Case record not found.
  > 
  > These propagate directly up through JDBC to display helpful dialogs in our UI.
  > 
  > Now, **Avika** will demonstrate these features live in the application and present our analytical SQL queries."*

---

### **PART 3: Avika Tyagi (25BCE1294)**
**Domain:** Analytical SQL, Recursive Hierarchical Queries, Java Swing GUI & Live Demonstration

#### 1. Complex Analytical SQL Queries
* **Visual Cue:** SQL Queries section (Pages 21–24) / 'Queries & Reports' Tab (Pages 44–45)
* **What to Say:**
  > *"Thank you, Sahana. To extract judicial insights from our database, I authored our analytical queries and connected the database to our Java Swing application.
  > 
  > Let's look at the key analytical queries we implemented:
  > * **Q1 (Multi-Table Inner Join):** Joins `COURT_CASE`, `COURT`, `HEARING`, and `JUDGE` to produce an active court calendar ordered by hearing date.
  > * **Q2 & Q3 (Aggregations with Grouping):** Q2 uses a `LEFT JOIN` with `GROUP BY` to calculate the case volume across all courts (including courts with zero cases). Q3 uses `GROUP BY` and a **`HAVING COUNT(*) >= 2`** filter to isolate overburdened judges.
  > * **Q4 & Q5 (Precedent Ranking & Ternary Resolution):** Q4 counts citation references to rank the most influential judicial precedents in descending order. Q5 resolves our ternary junction table `REPRESENTS` to show which advocate represents which litigant for a specific case.
  > * **Q6 (Subquery):** Filters cases that invoked a specific statute—such as Article 21—using a nested `WHERE Case_No IN (SELECT ...)` clause."*

#### 2. Hierarchical Traversal with CONNECT BY
* **Visual Cue:** Query 7 code & output (Pages 24, 26)
* **What to Say:**
  > *"The most critical query is **Query 7: Recursive Precedent Chain Traversal**.
  > 
  > Legal precedent is inherently hierarchical. In relational SQL, recursive graph querying requires Oracle's hierarchical syntax:
  > ```sql
  > SELECT LEVEL AS depth,
  >        LPAD(' ', 2*(LEVEL-1)) || c.Case_Title AS precedent_chain
  > FROM CITATION ci JOIN COURT_CASE c ON c.Case_No = ci.Cited_Case_No
  > START WITH ci.Citing_Case_No = 'SC-2018-05'
  > CONNECT BY PRIOR ci.Cited_Case_No = ci.Citing_Case_No;
  > ```
  > Using the `LEVEL` pseudo-column and `LPAD`, we formatted the indented precedent hierarchy. While this works in Oracle, hierarchical queries in relational tables are computationally expensive, require full table rescans as citation chains deepen, and struggle with multi-path cyclic traversals. This exact limitation is our motivation for **Phase 3 (DA3)**, where we will transition this citation table into a native graph database."*

#### 3. Java Swing Frontend Architecture & JDBC Safety
* **Visual Cue:** `DBConnection.java` / Class Hierarchy in VSCode/IDE (Pages 37–40)
* **What to Say:**
  > *"To interact with the database, I developed a Java Swing application organized into specialized tabbed panels:
  > * A centralized `DBConnection` class establishes connections using `DriverManager.getConnection()`.
  > * **SQL Injection Prevention:** Every user input panel uses `PreparedStatement` with typed parameters or `CallableStatement` for PL/SQL routines. Dynamic SQL concatenation is completely avoided.
  > * The only place raw SQL is permitted is in our dedicated **SQL Console tab**, provided specifically for examiners to test ad-hoc DDL/DML statements."*

#### 4. Live Demonstration Steps (Perform Live)
* **Visual Cue:** Running Java Application Window (Pages 41–49)
* **Action & Script:**
  * **Test Case 1: Trigger & Date Validation**
    > *(Action: Go to 'Hearings' tab, choose case `DC-2024-501`, set Hearing Date to `2020-01-01` (before filing date `2024-03-22`), and click 'Schedule Hearing').*  
    > *"Observe: the application does not crash. Trigger `trg_hearing_date_check` fires, Oracle raises `-20001`, and our UI displays: **'Invalid Hearing Date: cannot be before case filing date'**."*
  * **Test Case 2: Stored Procedure & Automatic Audit Trail**
    > *(Action: Go to 'Functions & Procedures' tab, invoke `update_case_status` for case `DC-2024-501` to `'Closed'`. Switch to 'SQL Console' tab, type `SELECT * FROM AUDIT_LOG ORDER BY Changed_On DESC;` and click Execute).*  
    > *"The update executed atomically, and `trg_status_audit` automatically inserted the old status, new status, and system timestamp into the `AUDIT_LOG` table."*
  * **Test Case 3: Interactive Analytical Reports**
    > *(Action: Go to 'Queries & Reports' tab, click '4. Most Cited Cases', then click '7. Citation Chain').*  
    > *"All seven analytical queries execute through JDBC and render directly into formatted Swing `JTable` components."*

#### 5. Conclusion & DA3 Roadmap
* **Visual Cue:** Conclusion Section (Page 50)
* **What to Say:**
  > *"To conclude:
  > 1. We designed and implemented a BCNF-compliant 12-table relational schema for court case tracking.
  > 2. We enforced business logic and audit integrity through PL/SQL procedures, functions, ref cursors, and triggers.
  > 3. We verified system integrity through analytical queries and a secure, injection-safe Java Swing desktop interface.
  > 
  > In **Phase 3 (DA3)**, we will take the relational `CITATION` table developed here and import it into Neo4j as a graph network. This will allow us to run graph algorithms—such as PageRank and shortest citation paths—over complex precedent networks with $O(1)$ relationship traversals.
  > 
  > Thank you. We welcome your questions."*

---

## 3. Anticipated Viva Q&A by Team Member

### Questions for Kanak Bansal:
1. **Q: Why is BCNF required instead of stopping at 3NF?**  
   * **Answer:** *"In 3NF, transitive dependencies on non-prime attributes are eliminated, but redundancies can persist if there are overlapping candidate keys. BCNF ensures that for every functional dependency $X \to Y$, $X$ must be a superkey, eliminating all update anomalies."*
2. **Q: Why did you separate `CASE_PARTY` and `REPRESENTS`?**  
   * **Answer:** *"`CASE_PARTY` defines which litigants are parties in a case. `REPRESENTS` is a ternary relationship that captures which specific advocate represents which party in that specific case, allowing lawyers to represent different clients across different matters without redundancy."*
3. **Q: Why does `AUDIT_LOG` lack foreign keys?**  
   * **Answer:** *"If a case record is purged or deleted, a foreign key would either cascade delete the audit entry or block deletion. Omitting the FK guarantees that the compliance history survives even after parent records are removed."*

### Questions for Sahana Karthikeyan:
1. **Q: What is the advantage of `SYS_REFCURSOR` over standard explicit cursors?**  
   * **Answer:** *"A standard explicit cursor is scoped strictly within the PL/SQL block. A `SYS_REFCURSOR` can be opened in Oracle and returned as an active pointer to external client applications like Java JDBC, enabling efficient streaming of large result sets."*
2. **Q: Why implement `trg_hearing_date_check` as a trigger instead of a CHECK constraint?**  
   * **Answer:** *"A table `CHECK` constraint can only evaluate column values within its own row. Cross-table validation—comparing `HEARING.Hearing_Date` with `COURT_CASE.Filing_Date`—requires a row-level trigger with query access."*
3. **Q: What is the purpose of `SQL%NOTFOUND` in `update_case_status`?**  
   * **Answer:** *"An `UPDATE` statement that matches zero rows does not raise an implicit `NO_DATA_FOUND` exception. Checking `SQL%NOTFOUND` allows us to explicitly catch non-existent case IDs and raise error `-20005`."*

### Questions for Avika Tyagi:
1. **Q: How does Oracle's `CONNECT BY PRIOR` construct the hierarchy in Query 7?**  
   * **Answer:** *"`START WITH` identifies the root node of the hierarchy (the initial citing case). `CONNECT BY PRIOR ci.Cited_Case_No = ci.Citing_Case_No` tells the query engine that the child's citing case must match the parent's cited case, recursively traversing the DAG level-by-level."*
2. **Q: Why is recursive querying inefficient in relational SQL compared to a Graph Database?**  
   * **Answer:** *"Relational engines implement `CONNECT BY` or recursive CTEs via repeated index lookups or nested self-joins, causing query cost to grow exponentially with chain depth. Graph databases like Neo4j use index-free adjacency, where relationship traversals are constant-time $O(1)$ operations."*
3. **Q: How did you safeguard the Java application against SQL Injection?**  
   * **Answer:** *"All data-entry tabs use parameterized `PreparedStatement` and `CallableStatement`. Parameters are transmitted out-of-band using typed setters (e.g. `setString`, `setInt`), ensuring user input is never interpreted as executable SQL syntax."*
