package util;

import db.DBConnection;
import db.neo4j.Neo4jConnection;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MigrateOracleToNeo4j - Migration utility that extracts data from Oracle RDBMS tables via JDBC
 * and migrates entities into Neo4j Property Graph nodes and relationships.
 * Demonstrates relational-to-graph transformation, batching with UNWIND, and migration validation.
 */
public class MigrateOracleToNeo4j {

    public static class MigrationReport {
        public int courtsCount = 0;
        public int judgesCount = 0;
        public int casesCount = 0;
        public int lawyersCount = 0;
        public int partiesCount = 0;
        public int statutesCount = 0;
        public int hearingsCount = 0;
        public int citationsCount = 0;
        public int representsCount = 0;
        public int casePartiesCount = 0;
        public int caseStatutesCount = 0;
        public boolean success = false;
        public String message = "";

        @Override
        public String toString() {
            return String.format(
                "Migration Report:\n" +
                "  - Court Nodes: %d\n" +
                "  - Judge Nodes: %d (with :SITS_IN edges)\n" +
                "  - Case Nodes: %d (with :FILED_IN edges)\n" +
                "  - Lawyer Nodes: %d\n" +
                "  - Party Nodes: %d\n" +
                "  - Statute Nodes: %d\n" +
                "  - Hearing Nodes: %d (with :HAS_HEARING & :PRESIDED edges)\n" +
                "  - Citation Relationships [:CITES]: %d\n" +
                "  - Representation Relationships [:REPRESENTS]: %d\n" +
                "  - Party-Case Relationships [:PARTY_TO]: %d\n" +
                "  - Case-Statute Relationships [:INVOKES]: %d\n" +
                "Status: %s",
                courtsCount, judgesCount, casesCount, lawyersCount, partiesCount, statutesCount,
                hearingsCount, citationsCount, representsCount, casePartiesCount, caseStatutesCount,
                success ? "SUCCESSFUL" : "FAILED: " + message
            );
        }
    }

    public static MigrationReport runMigration() {
        MigrationReport report = new MigrationReport();

        if (!Neo4jConnection.testConnection()) {
            report.success = false;
            report.message = "Could not connect to Neo4j database! Please ensure Neo4j is running on bolt://localhost:7687";
            return report;
        }

        try (Connection oConn = DBConnection.getConnection()) {
            System.out.println("Oracle JDBC Connection established. Starting migration to Neo4j...");

            try (Session nSession = Neo4jConnection.getSession();
                 Statement stmt = oConn.createStatement()) {

                // 1. Migrate COURT -> (:Court)
                try (ResultSet rs = stmt.executeQuery("SELECT Court_ID, Court_Name, Court_Level, Location FROM COURT")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Court_ID"));
                        m.put("name", rs.getString("Court_Name"));
                        m.put("level", rs.getString("Court_Level"));
                        m.put("loc", rs.getString("Location"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (co:Court {court_id: row.id}) " +
                            "SET co.court_name = row.name, co.court_level = row.level, co.location = row.loc",
                            Values.parameters("batch", batch)
                        );
                        report.courtsCount = batch.size();
                    }
                }

                // 2. Migrate JUDGE -> (:Judge)-[:SITS_IN]->(:Court)
                try (ResultSet rs = stmt.executeQuery("SELECT Judge_ID, Judge_Name, Designation, Court_ID FROM JUDGE")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Judge_ID"));
                        m.put("name", rs.getString("Judge_Name"));
                        m.put("desig", rs.getString("Designation"));
                        m.put("court_id", rs.getInt("Court_ID"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (j:Judge {judge_id: row.id}) " +
                            "SET j.judge_name = row.name, j.designation = row.desig " +
                            "WITH j, row " +
                            "WHERE row.court_id > 0 " +
                            "MATCH (co:Court {court_id: row.court_id}) " +
                            "MERGE (j)-[:SITS_IN]->(co)",
                            Values.parameters("batch", batch)
                        );
                        report.judgesCount = batch.size();
                    }
                }

                // 3. Migrate COURT_CASE -> (:Case)-[:FILED_IN]->(:Court)
                try (ResultSet rs = stmt.executeQuery("SELECT Case_No, Case_Title, TO_CHAR(Filing_Date, 'YYYY-MM-DD') AS FDate, Case_Type, Status, Court_ID FROM COURT_CASE")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("case_no", rs.getString("Case_No"));
                        m.put("title", rs.getString("Case_Title"));
                        m.put("fdate", rs.getString("FDate"));
                        m.put("type", rs.getString("Case_Type"));
                        m.put("status", rs.getString("Status"));
                        m.put("court_id", rs.getInt("Court_ID"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (c:Case {case_no: row.case_no}) " +
                            "SET c.title = row.title, c.filing_date = row.fdate, c.case_type = row.type, c.status = row.status " +
                            "WITH c, row " +
                            "WHERE row.court_id > 0 " +
                            "MATCH (co:Court {court_id: row.court_id}) " +
                            "MERGE (c)-[:FILED_IN]->(co)",
                            Values.parameters("batch", batch)
                        );
                        report.casesCount = batch.size();
                    }
                }

                // 4. Migrate LAWYER -> (:Lawyer)
                try (ResultSet rs = stmt.executeQuery("SELECT Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization FROM LAWYER")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Lawyer_ID"));
                        m.put("name", rs.getString("Lawyer_Name"));
                        m.put("bar", rs.getString("Bar_Reg_No"));
                        m.put("spec", rs.getString("Specialization"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (l:Lawyer {lawyer_id: row.id}) " +
                            "SET l.lawyer_name = row.name, l.bar_reg_no = row.bar, l.specialization = row.spec",
                            Values.parameters("batch", batch)
                        );
                        report.lawyersCount = batch.size();
                    }
                }

                // 5. Migrate PARTY -> (:Party)
                try (ResultSet rs = stmt.executeQuery("SELECT Party_ID, Party_Name, Contact_No FROM PARTY")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Party_ID"));
                        m.put("name", rs.getString("Party_Name"));
                        m.put("contact", rs.getString("Contact_No"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (p:Party {party_id: row.id}) " +
                            "SET p.party_name = row.name, p.contact_no = row.contact",
                            Values.parameters("batch", batch)
                        );
                        report.partiesCount = batch.size();
                    }
                }

                // 6. Migrate STATUTE -> (:Statute)
                try (ResultSet rs = stmt.executeQuery("SELECT Statute_ID, Act_Name, Section_No, Description FROM STATUTE")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Statute_ID"));
                        m.put("act", rs.getString("Act_Name"));
                        m.put("sec", rs.getString("Section_No"));
                        m.put("desc", rs.getString("Description"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (s:Statute {statute_id: row.id}) " +
                            "SET s.act_name = row.act, s.section_no = row.sec, s.description = row.desc",
                            Values.parameters("batch", batch)
                        );
                        report.statutesCount = batch.size();
                    }
                }

                // 7. Migrate HEARING -> (:Hearing), (Case)-[:HAS_HEARING]->(Hearing), (Judge)-[:PRESIDED]->(Hearing)
                try (ResultSet rs = stmt.executeQuery("SELECT Hearing_ID, Case_No, Judge_ID, TO_CHAR(Hearing_Date, 'YYYY-MM-DD') AS HDate, Remarks FROM HEARING")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", rs.getInt("Hearing_ID"));
                        m.put("case_no", rs.getString("Case_No"));
                        m.put("judge_id", rs.getInt("Judge_ID"));
                        m.put("hdate", rs.getString("HDate"));
                        m.put("remarks", rs.getString("Remarks"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MERGE (h:Hearing {hearing_id: row.id}) " +
                            "SET h.hearing_date = row.hdate, h.remarks = row.remarks " +
                            "WITH h, row " +
                            "MATCH (c:Case {case_no: row.case_no}) " +
                            "MERGE (c)-[:HAS_HEARING]->(h) " +
                            "WITH h, row " +
                            "WHERE row.judge_id > 0 " +
                            "MATCH (j:Judge {judge_id: row.judge_id}) " +
                            "MERGE (j)-[:PRESIDED]->(h)",
                            Values.parameters("batch", batch)
                        );
                        report.hearingsCount = batch.size();
                    }
                }

                // 8. Migrate CASE_PARTY -> (:Party)-[:PARTY_TO]->(:Case)
                try (ResultSet rs = stmt.executeQuery("SELECT Case_No, Party_ID FROM CASE_PARTY")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("case_no", rs.getString("Case_No"));
                        m.put("party_id", rs.getInt("Party_ID"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MATCH (p:Party {party_id: row.party_id}) " +
                            "MATCH (c:Case {case_no: row.case_no}) " +
                            "MERGE (p)-[:PARTY_TO]->(c)",
                            Values.parameters("batch", batch)
                        );
                        report.casePartiesCount = batch.size();
                    }
                }

                // 9. Migrate CASE_STATUTE -> (:Case)-[:INVOKES]->(:Statute)
                try (ResultSet rs = stmt.executeQuery("SELECT Case_No, Statute_ID FROM CASE_STATUTE")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("case_no", rs.getString("Case_No"));
                        m.put("statute_id", rs.getInt("Statute_ID"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MATCH (c:Case {case_no: row.case_no}) " +
                            "MATCH (s:Statute {statute_id: row.statute_id}) " +
                            "MERGE (c)-[:INVOKES]->(s)",
                            Values.parameters("batch", batch)
                        );
                        report.caseStatutesCount = batch.size();
                    }
                }

                // 10. Migrate REPRESENTS -> (:Lawyer)-[:REPRESENTS {role, case_no}]->(:Party)
                try (ResultSet rs = stmt.executeQuery("SELECT Lawyer_ID, Party_ID, Case_No, Role FROM REPRESENTS")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("lawyer_id", rs.getInt("Lawyer_ID"));
                        m.put("party_id", rs.getInt("Party_ID"));
                        m.put("case_no", rs.getString("Case_No"));
                        m.put("role", rs.getString("Role"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MATCH (l:Lawyer {lawyer_id: row.lawyer_id}) " +
                            "MATCH (p:Party {party_id: row.party_id}) " +
                            "MERGE (l)-[r:REPRESENTS {case_no: row.case_no}]->(p) " +
                            "SET r.role = row.role",
                            Values.parameters("batch", batch)
                        );
                        report.representsCount = batch.size();
                    }
                }

                // 11. Migrate CITATION -> (:Case)-[:CITES {citation_date}]->(:Case)
                try (ResultSet rs = stmt.executeQuery("SELECT Citing_Case_No, Cited_Case_No, TO_CHAR(Citation_Date, 'YYYY-MM-DD') AS CDate FROM CITATION")) {
                    List<Map<String, Object>> batch = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("citing", rs.getString("Citing_Case_No"));
                        m.put("cited", rs.getString("Cited_Case_No"));
                        m.put("cdate", rs.getString("CDate"));
                        batch.add(m);
                    }
                    if (!batch.isEmpty()) {
                        nSession.run(
                            "UNWIND $batch AS row " +
                            "MATCH (c1:Case {case_no: row.citing}) " +
                            "MATCH (c2:Case {case_no: row.cited}) " +
                            "MERGE (c1)-[r:CITES]->(c2) " +
                            "SET r.citation_date = row.cdate",
                            Values.parameters("batch", batch)
                        );
                        report.citationsCount = batch.size();
                    }
                }

                report.success = true;
                report.message = "All Oracle tables successfully migrated to Neo4j graph!";
            }
        } catch (Exception e) {
            report.success = false;
            report.message = "Migration Error: " + e.getMessage();
        }

        return report;
    }

    public static void main(String[] args) {
    System.out.println("Executing Oracle to Neo4j Migration Utility...");
    System.out.println("Starting migration...");

    MigrationReport report = runMigration();

    System.out.println("Migration finished.");
    System.out.println(report.toString());
}
}
