package dao.graph;

import db.neo4j.Neo4jConnection;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphCrudDAO - Data Access Object for Core Property Graph CRUD operations in Neo4j.
 * Demonstrates node creation, relationship management, property updates, and DETACH DELETE.
 * All Cypher queries are strictly parameterized.
 */
public class GraphCrudDAO {

    /**
     * Creates or updates a Case node and links it to its Court.
     */
    public void createOrUpdateCase(String caseNo, String title, LocalDate filingDate, String caseType, String status, int courtId) {
        String cypher =
            "MERGE (c:Case {case_no: $case_no}) " +
            "SET c.title = $title, " +
            "    c.filing_date = $filing_date, " +
            "    c.case_type = $case_type, " +
            "    c.status = $status " +
            "WITH c " +
            "WHERE $court_id > 0 " +
            "MATCH (co:Court {court_id: $court_id}) " +
            "MERGE (c)-[:FILED_IN]->(co)";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "case_no", caseNo,
                "title", title,
                "filing_date", filingDate.toString(),
                "case_type", caseType,
                "status", status,
                "court_id", courtId
            ));
        }
    }

    /**
     * Updates the status of an existing Case node.
     */
    public boolean updateCaseStatus(String caseNo, String newStatus) {
        String cypher =
            "MATCH (c:Case {case_no: $case_no}) " +
            "SET c.status = $status " +
            "RETURN c.case_no AS updated_case";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_no", caseNo, "status", newStatus));
            return result.hasNext();
        }
    }

    /**
     * Deletes a Case node and all connected relationships using DETACH DELETE.
     */
    public boolean detachDeleteCase(String caseNo) {
        String cypher =
            "MATCH (c:Case {case_no: $case_no}) " +
            "DETACH DELETE c " +
            "RETURN count(c) AS deleted_count";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_no", caseNo));
            if (result.hasNext()) {
                return result.next().get("deleted_count").asInt() > 0;
            }
            return false;
        }
    }

    /**
     * Creates a directed CITES relationship from citing case to cited precedent.
     */
    public void createCitation(String citingCaseNo, String citedCaseNo, LocalDate citationDate) {
        String cypher =
            "MATCH (c1:Case {case_no: $citing}) " +
            "MATCH (c2:Case {case_no: $cited}) " +
            "MERGE (c1)-[r:CITES]->(c2) " +
            "ON CREATE SET r.citation_date = $cdate " +
            "ON MATCH SET r.citation_date = $cdate";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "citing", citingCaseNo,
                "cited", citedCaseNo,
                "cdate", citationDate != null ? citationDate.toString() : LocalDate.now().toString()
            ));
        }
    }

    /**
     * Deletes a CITES relationship between two cases.
     */
    public boolean deleteCitation(String citingCaseNo, String citedCaseNo) {
        String cypher =
            "MATCH (c1:Case {case_no: $citing})-[r:CITES]->(c2:Case {case_no: $cited}) " +
            "DELETE r " +
            "RETURN count(r) AS deleted_count";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("citing", citingCaseNo, "cited", citedCaseNo));
            return result.hasNext() && result.next().get("deleted_count").asInt() > 0;
        }
    }

    /**
     * Creates a Court node.
     */
    public void createCourt(int courtId, String courtName, String courtLevel, String location) {
        String cypher =
            "MERGE (co:Court {court_id: $id}) " +
            "SET co.court_name = $name, co.court_level = $level, co.location = $location";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "id", courtId, "name", courtName, "level", courtLevel, "location", location
            ));
        }
    }

    /**
     * Creates a Judge node and links to a Court.
     */
    public void createJudge(int judgeId, String judgeName, String designation, int courtId) {
        String cypher =
            "MERGE (j:Judge {judge_id: $id}) " +
            "SET j.judge_name = $name, j.designation = $designation " +
            "WITH j " +
            "WHERE $court_id > 0 " +
            "MATCH (co:Court {court_id: $court_id}) " +
            "MERGE (j)-[:SITS_IN]->(co)";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "id", judgeId, "name", judgeName, "designation", designation, "court_id", courtId
            ));
        }
    }

    /**
     * Creates a Lawyer node.
     */
    public void createLawyer(int lawyerId, String lawyerName, String barRegNo, String specialization) {
        String cypher =
            "MERGE (l:Lawyer {lawyer_id: $id}) " +
            "SET l.lawyer_name = $name, l.bar_reg_no = $bar, l.specialization = $spec";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "id", lawyerId, "name", lawyerName, "bar", barRegNo, "spec", specialization
            ));
        }
    }

    /**
     * Creates a Party node.
     */
    public void createParty(int partyId, String partyName, String contactNo) {
        String cypher =
            "MERGE (p:Party {party_id: $id}) " +
            "SET p.party_name = $name, p.contact_no = $contact";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "id", partyId, "name", partyName, "contact", contactNo
            ));
        }
    }

    /**
     * Creates a Statute node.
     */
    public void createStatute(int statuteId, String actName, String sectionNo, String description) {
        String cypher =
            "MERGE (s:Statute {statute_id: $id}) " +
            "SET s.act_name = $act, s.section_no = $sec, s.description = $desc";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "id", statuteId, "act", actName, "sec", sectionNo, "desc", description
            ));
        }
    }

    /**
     * Creates an INVOKES relationship from Case to Statute.
     */
    public void createInvokes(String caseNo, int statuteId) {
        String cypher =
            "MATCH (c:Case {case_no: $case_no}) " +
            "MATCH (s:Statute {statute_id: $statute_id}) " +
            "MERGE (c)-[:INVOKES]->(s)";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters("case_no", caseNo, "statute_id", statuteId));
        }
    }

    /**
     * Creates a PARTY_TO relationship from Party to Case.
     */
    public void createPartyTo(int partyId, String caseNo) {
        String cypher =
            "MATCH (p:Party {party_id: $party_id}) " +
            "MATCH (c:Case {case_no: $case_no}) " +
            "MERGE (p)-[:PARTY_TO]->(c)";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters("party_id", partyId, "case_no", caseNo));
        }
    }

    /**
     * Creates a REPRESENTS relationship with properties {role, case_no}.
     */
    public void createRepresents(int lawyerId, int partyId, String caseNo, String role) {
        String cypher =
            "MATCH (l:Lawyer {lawyer_id: $lawyer_id}) " +
            "MATCH (p:Party {party_id: $party_id}) " +
            "MERGE (l)-[r:REPRESENTS {case_no: $case_no}]->(p) " +
            "SET r.role = $role";

        try (Session session = Neo4jConnection.getSession()) {
            session.run(cypher, Values.parameters(
                "lawyer_id", lawyerId, "party_id", partyId, "case_no", caseNo, "role", role
            ));
        }
    }

    /**
     * Retrieves all cases from Neo4j for dropdown lists.
     */
    public List<Map<String, String>> getAllCasesSummary() {
        List<Map<String, String>> list = new ArrayList<>();
        String cypher = "MATCH (c:Case) RETURN c.case_no AS case_no, c.title AS title ORDER BY c.case_no";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher);
            while (result.hasNext()) {
                Record r = result.next();
                Map<String, String> map = new HashMap<>();
                map.put("case_no", r.get("case_no").asString());
                map.put("title", r.get("title").asString(""));
                list.add(map);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
