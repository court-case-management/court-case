package dao.graph;

import db.neo4j.Neo4jConnection;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import javax.swing.table.DefaultTableModel;
import java.util.*;

/**
 * GraphQueryDAO - Executes the 10 Technology-Specific Cypher queries in Neo4j.
 * Converts graph traversal paths and projections into Swing TableModels and visual graph models.
 */
public class GraphQueryDAO {

    /**
     * Converts a Neo4j driver Result into a Swing DefaultTableModel dynamically.
     */
    public static DefaultTableModel resultToTableModel(Result result) {
        List<String> keys = result.keys();
        Vector<String> columnNames = new Vector<>(keys);
        Vector<Vector<Object>> data = new Vector<>();

        while (result.hasNext()) {
            Record record = result.next();
            Vector<Object> row = new Vector<>();
            for (String key : keys) {
                org.neo4j.driver.Value val = record.get(key);
                if (val.isNull()) {
                    row.add("null");
                } else if (val.type().name().equals("LIST")) {
                    row.add(val.asList());
                } else {
                    row.add(val.asObject());
                }
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /**
     * QUERY 1: Full precedent chain of a case (all cases it directly or indirectly relies on).
     */
    public DefaultTableModel getFullPrecedentChain(String caseNo) {
        String cypher =
            "MATCH path = (c:Case {case_no: $case_no})-[:CITES*1..]->(precedent:Case) " +
            "WITH precedent, length(path) AS depth, [x IN nodes(path) | x.case_no] AS citation_path " +
            "RETURN precedent.case_no AS Precedent_No, " +
            "       precedent.title AS Precedent_Title, " +
            "       precedent.filing_date AS Filing_Date, " +
            "       min(depth) AS Min_Hop_Distance, " +
            "       citation_path AS Full_Path " +
            "ORDER BY Min_Hop_Distance ASC, Precedent_No ASC";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_no", caseNo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 2: Downstream cases that depend on a given case (reverse citation navigation).
     */
    public DefaultTableModel getDownstreamDependentCases(String caseNo) {
        String cypher =
            "MATCH path = (subsequent:Case)-[:CITES*1..]->(landmark:Case {case_no: $case_no}) " +
            "RETURN subsequent.case_no AS Dependent_Case_No, " +
            "       subsequent.title AS Dependent_Title, " +
            "       subsequent.case_type AS Case_Type, " +
            "       length(path) AS Chain_Depth, " +
            "       [x IN nodes(path) | x.case_no] AS Traversal_Route " +
            "ORDER BY Chain_Depth ASC, subsequent.filing_date ASC";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_no", caseNo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 3: Shortest citation path between two cases using shortestPath().
     */
    public DefaultTableModel getShortestCitationPath(String caseFrom, String caseTo) {
        String cypher =
            "MATCH (c1:Case {case_no: $case_from}), (c2:Case {case_no: $case_to}) " +
            "MATCH path = shortestPath((c1)-[:CITES*]-(c2)) " +
            "RETURN length(path) AS Hop_Length, " +
            "       [n IN nodes(path) | n.case_no + ': ' + n.title] AS Precedent_Trail";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_from", caseFrom, "case_to", caseTo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 4: Most influential landmark cases by in-degree citation count.
     */
    public DefaultTableModel getMostInfluentialCases() {
        String cypher =
            "MATCH (c:Case) " +
            "OPTIONAL MATCH (citing:Case)-[:CITES]->(c) " +
            "WITH c, count(citing) AS Times_Cited " +
            "ORDER BY Times_Cited DESC " +
            "RETURN c.case_no AS Case_No, " +
            "       c.title AS Landmark_Title, " +
            "       c.case_type AS Category, " +
            "       c.status AS Status, " +
            "       Times_Cited " +
            "LIMIT 20";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher);
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 5: Co-cited cases (pairs of cases cited together).
     */
    public DefaultTableModel getCoCitedCases() {
        String cypher =
            "MATCH (citing:Case)-[:CITES]->(c1:Case) " +
            "MATCH (citing)-[:CITES]->(c2:Case) " +
            "WHERE id(c1) < id(c2) " +
            "WITH c1, c2, count(citing) AS Co_Citation_Count " +
            "WHERE Co_Citation_Count >= 2 " +
            "RETURN c1.case_no AS Precedent_A, " +
            "       c1.title AS Title_A, " +
            "       c2.case_no AS Precedent_B, " +
            "       c2.title AS Title_B, " +
            "       Co_Citation_Count " +
            "ORDER BY Co_Citation_Count DESC " +
            "LIMIT 20";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher);
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 6: Cases similar to a target case based on shared statutes and precedents.
     */
    public DefaultTableModel getSimilarCases(String caseNo) {
        String cypher =
            "MATCH (target:Case {case_no: $target_case}) " +
            "MATCH (target)-[:INVOKES]->(s:Statute)<-[:INVOKES]-(other:Case) " +
            "WHERE other <> target " +
            "OPTIONAL MATCH (target)-[:CITES]->(prec:Case)<-[:CITES]-(other) " +
            "WITH target, other, " +
            "     count(DISTINCT s) AS Shared_Statutes, " +
            "     count(DISTINCT prec) AS Shared_Precedents " +
            "WITH other, " +
            "     Shared_Statutes, " +
            "     Shared_Precedents, " +
            "     (Shared_Statutes * 2.0 + Shared_Precedents * 3.0) AS Similarity_Score " +
            "RETURN other.case_no AS Similar_Case_No, " +
            "       other.title AS Case_Title, " +
            "       other.case_type AS Case_Type, " +
            "       Shared_Statutes, " +
            "       Shared_Precedents, " +
            "       Similarity_Score " +
            "ORDER BY Similarity_Score DESC " +
            "LIMIT 10";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("target_case", caseNo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 7: Judges presiding over cases invoking a particular statute.
     */
    public DefaultTableModel getJudgesByStatute(String actName, String sectionNo) {
        String cypher =
            "MATCH (j:Judge)-[:PRESIDED]->(h:Hearing)<-[:HAS_HEARING]-(c:Case)-[:INVOKES]->(s:Statute) " +
            "WHERE s.act_name = $act AND s.section_no = $sec " +
            "RETURN j.judge_id AS Judge_ID, " +
            "       j.judge_name AS Judge_Name, " +
            "       j.designation AS Designation, " +
            "       count(DISTINCT c) AS Cases_Heard, " +
            "       collect(DISTINCT c.case_no) AS Case_Numbers " +
            "ORDER BY Cases_Heard DESC";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("act", actName, "sec", sectionNo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 8: Lawyers representing parties in cases citing a landmark case.
     */
    public DefaultTableModel getLawyersInvokingLandmark(String landmarkCaseNo) {
        String cypher =
            "MATCH (l:Lawyer)-[rep:REPRESENTS]->(p:Party)-[:PARTY_TO]->(c:Case)-[:CITES]->(landmark:Case {case_no: $landmark_case}) " +
            "RETURN DISTINCT l.lawyer_name AS Lawyer_Name, " +
            "       l.bar_reg_no AS Bar_Reg_No, " +
            "       l.specialization AS Specialization, " +
            "       p.party_name AS Client_Party, " +
            "       rep.role AS Representation_Role, " +
            "       c.case_no AS Case_Invoking_Landmark, " +
            "       c.title AS Case_Title";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("landmark_case", landmarkCaseNo));
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 9: Citation depth and classification of isolated nodes vs root precedents.
     */
    public DefaultTableModel getCitationDepthAndClassification() {
        String cypher =
            "MATCH (c:Case) " +
            "OPTIONAL MATCH path = (c)-[:CITES*1..5]->(ancestor:Case) " +
            "WITH c, max(coalesce(length(path), 0)) AS Max_Precedent_Depth " +
            "OPTIONAL MATCH (c)<-[:CITES]-(citing:Case) " +
            "WITH c, Max_Precedent_Depth, count(citing) AS In_Degree " +
            "RETURN c.case_no AS Case_No, " +
            "       c.title AS Case_Title, " +
            "       Max_Precedent_Depth AS Max_Citation_Depth, " +
            "       In_Degree AS Downstream_Citations, " +
            "       CASE " +
            "         WHEN Max_Precedent_Depth = 0 AND In_Degree = 0 THEN 'Isolated Node' " +
            "         WHEN Max_Precedent_Depth = 0 THEN 'Root Precedent' " +
            "         WHEN In_Degree = 0 THEN 'Terminal Leaf' " +
            "         ELSE 'Active Intermediate Bridge' " +
            "       END AS Graph_Classification " +
            "ORDER BY In_Degree DESC, Max_Precedent_Depth DESC " +
            "LIMIT 25";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher);
            return resultToTableModel(result);
        }
    }

    /**
     * QUERY 10: Cross-court influence (High/District Court cases citing Supreme Court cases).
     */
    public DefaultTableModel getCrossCourtInfluence() {
        String cypher =
            "MATCH (subordinate_case:Case)-[:FILED_IN]->(sub_court:Court) " +
            "WHERE sub_court.court_level IN ['High', 'District'] " +
            "MATCH (subordinate_case)-[:CITES]->(supreme_case:Case)-[:FILED_IN]->(sc:Court {court_level: 'Supreme'}) " +
            "RETURN sub_court.court_level AS Originating_Court_Level, " +
            "       sub_court.court_name AS Originating_Court, " +
            "       count(DISTINCT subordinate_case) AS Cases_Citing_Supreme_Court, " +
            "       collect(DISTINCT supreme_case.case_no) AS Supreme_Court_Precedents_Cited " +
            "ORDER BY Originating_Court_Level ASC, Cases_Citing_Supreme_Court DESC";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher);
            return resultToTableModel(result);
        }
    }

    /**
     * Executes arbitrary Cypher query from Cypher Console and returns TableModel.
     */
    public DefaultTableModel executeArbitraryCypher(String cypherQuery) {
        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypherQuery);
            return resultToTableModel(result);
        }
    }

    /**
     * Extracts neighborhood graph nodes and citation edges around a case for visual rendering.
     */
    public Map<String, Object> getNeighborhoodGraph(String caseNo, int hops) {
        Map<String, Object> graphData = new HashMap<>();
        Set<String> nodeIds = new HashSet<>();
        List<Map<String, String>> nodesList = new ArrayList<>();
        List<Map<String, String>> edgesList = new ArrayList<>();

        String cypher =
            "MATCH (root:Case {case_no: $case_no}) " +
            "CALL { " +
            "  WITH root " +
            "  MATCH path = (root)-[:CITES*1.." + hops + "]->(cited:Case) " +
            "  RETURN path " +
            "  UNION " +
            "  WITH root " +
            "  MATCH path = (citing:Case)-[:CITES*1.." + hops + "]->(root) " +
            "  RETURN path " +
            "} " +
            "UNWIND nodes(path) AS n " +
            "UNWIND relationships(path) AS r " +
            "RETURN DISTINCT n.case_no AS case_no, n.title AS title, " +
            "       startNode(r).case_no AS source, endNode(r).case_no AS target";

        try (Session session = Neo4jConnection.getSession()) {
            Result result = session.run(cypher, Values.parameters("case_no", caseNo));
            while (result.hasNext()) {
                Record rec = result.next();
                String cNo = rec.get("case_no").asString();
                String title = rec.get("title").asString("");
                if (!nodeIds.contains(cNo)) {
                    nodeIds.add(cNo);
                    Map<String, String> nMap = new HashMap<>();
                    nMap.put("id", cNo);
                    nMap.put("title", title);
                    nodesList.add(nMap);
                }

                String src = rec.get("source").asString(null);
                String tgt = rec.get("target").asString(null);
                if (src != null && tgt != null) {
                    Map<String, String> eMap = new HashMap<>();
                    eMap.put("source", src);
                    eMap.put("target", tgt);
                    edgesList.add(eMap);
                }
            }
        } catch (Exception ignored) {}

        // Ensure root case is always included
        if (!nodeIds.contains(caseNo)) {
            Map<String, String> rootMap = new HashMap<>();
            rootMap.put("id", caseNo);
            rootMap.put("title", caseNo);
            nodesList.add(rootMap);
        }

        graphData.put("nodes", nodesList);
        graphData.put("edges", edgesList);
        return graphData;
    }
}
