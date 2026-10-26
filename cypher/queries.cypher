// =============================================================================
// Court Case Management System - 10 Technology-Specific Cypher Queries (DA3)
// =============================================================================
// All queries are parameterized (e.g. $case_no, $statute_act) for performance
// and protection against Cypher injection.
// =============================================================================

// -----------------------------------------------------------------------------
// QUERY 1: Full Precedent Chain of a Case (Variable-Length Path [:CITES*1..])
// Finds all cases that the given case directly or transitively relies upon.
// Demonstrates: Variable-length relationship traversal [:CITES*1..] and path functions.
// -----------------------------------------------------------------------------
// Parameters: { "case_no": "SC-2024-02" }
MATCH path = (c:Case {case_no: $case_no})-[:CITES*1..]->(precedent:Case)
WITH precedent, length(path) AS depth, [x IN nodes(path) | x.case_no] AS citation_path
RETURN precedent.case_no AS Precedent_No,
       precedent.title AS Precedent_Title,
       precedent.filing_date AS Filing_Date,
       min(depth) AS Min_Hop_Distance,
       citation_path AS Full_Path
ORDER BY Min_Hop_Distance ASC, Precedent_No ASC;

// -----------------------------------------------------------------------------
// QUERY 2: Downstream Dependent Cases (Reverse Transitive Citations)
// Finds all subsequent cases that cite the target landmark case directly or indirectly.
// Demonstrates: Reverse relationship navigation <-[:CITES*1..]-.
// -----------------------------------------------------------------------------
// Parameters: { "case_no": "SC-1973-01" } // Kesavananda Bharati
MATCH path = (subsequent:Case)-[:CITES*1..]->(landmark:Case {case_no: $case_no})
RETURN subsequent.case_no AS Dependent_Case_No,
       subsequent.title AS Dependent_Title,
       subsequent.case_type AS Case_Type,
       length(path) AS Chain_Depth,
       [x IN nodes(path) | x.case_no] AS Traversal_Route
ORDER BY Chain_Depth ASC, subsequent.filing_date ASC;

// -----------------------------------------------------------------------------
// QUERY 3: Shortest Citation Path Between Two Cases (shortestPath)
// Finds the shortest precedent bridge connecting two legal cases across jurisprudence.
// Demonstrates: Graph algorithm shortestPath().
// -----------------------------------------------------------------------------
// Parameters: { "case_from": "SC-2024-02", "case_to": "SC-1950-08" }
MATCH (c1:Case {case_no: $case_from}), (c2:Case {case_no: $case_to})
MATCH path = shortestPath((c1)-[:CITES*]-(c2))
RETURN length(path) AS Hop_Length,
       [n IN nodes(path) | n.case_no + ': ' + n.title] AS Precedent_Trail,
       relationships(path) AS Relationships;

// -----------------------------------------------------------------------------
// QUERY 4: Most Influential Landmark Cases (In-Degree Citation Centrality)
// Calculates how many times each case is cited as precedent by other cases.
// Demonstrates: Pattern aggregation count(), degree centrality, and GDS fallback.
// -----------------------------------------------------------------------------
MATCH (c:Case)
OPTIONAL MATCH (citing:Case)-[:CITES]->(c)
WITH c, count(citing) AS Times_Cited
ORDER BY Times_Cited DESC
RETURN c.case_no AS Case_No,
       c.title AS Landmark_Title,
       c.case_type AS Category,
       c.status AS Status,
       Times_Cited
LIMIT 15;

// Optional GDS PageRank version (if GDS library is loaded):
// CALL gds.pageRank.stream('caseCitationGraph')
// YIELD nodeId, score
// RETURN gds.util.asNode(nodeId).case_no AS Case_No, score
// ORDER BY score DESC LIMIT 15;

// -----------------------------------------------------------------------------
// QUERY 5: Co-Cited Cases (Frequently Cited Together)
// Identifies pairs of precedents frequently cited together by subsequent judgments,
// revealing legal doctrines that complement each other.
// Demonstrates: Multiple pattern match on common citing node with inequality filter.
// -----------------------------------------------------------------------------
MATCH (citing:Case)-[:CITES]->(c1:Case)
MATCH (citing)-[:CITES]->(c2:Case)
WHERE id(c1) < id(c2) // Avoid reflexive and duplicate mirrored pairs
WITH c1, c2, count(citing) AS Co_Citation_Count, collect(citing.case_no) AS Citing_Judgments
WHERE Co_Citation_Count >= 2
RETURN c1.case_no AS Precedent_A,
       c1.title AS Title_A,
       c2.case_no AS Precedent_B,
       c2.title AS Title_B,
       Co_Citation_Count,
       Co_Citation_Count * 1.0 AS Relevance_Score
ORDER BY Co_Citation_Count DESC
LIMIT 15;

// -----------------------------------------------------------------------------
// QUERY 6: Cases Similar to a Target Case (Shared Statutes & Shared Precedents)
// Computes legal similarity based on Jaccard-like overlap of invoked statutes
// and cited precedent authorities.
// Demonstrates: Graph-based semantic similarity and set intersection scoring.
// -----------------------------------------------------------------------------
// Parameters: { "target_case": "SC-2018-05" } // Puttaswamy Privacy Case
MATCH (target:Case {case_no: $target_case})
MATCH (target)-[:INVOKES]->(s:Statute)<-[:INVOKES]-(other:Case)
WHERE other <> target
OPTIONAL MATCH (target)-[:CITES]->(prec:Case)<-[:CITES]-(other)
WITH target, other,
     count(DISTINCT s) AS Shared_Statutes,
     count(DISTINCT prec) AS Shared_Precedents
WITH other,
     Shared_Statutes,
     Shared_Precedents,
     (Shared_Statutes * 2.0 + Shared_Precedents * 3.0) AS Similarity_Score
RETURN other.case_no AS Similar_Case_No,
       other.title AS Case_Title,
       other.case_type AS Case_Type,
       Shared_Statutes,
       Shared_Precedents,
       Similarity_Score
ORDER BY Similarity_Score DESC
LIMIT 10;

// -----------------------------------------------------------------------------
// QUERY 7: Judges Presiding Over Cases Invoking Specific Statute
// Multi-hop traversal: Judge -> Hearing -> Case -> Statute.
// Demonstrates: Long-chain traversal across 4 distinct entity types without SQL joins.
// -----------------------------------------------------------------------------
// Parameters: { "statute_act": "Constitution of India", "section_no": "Article 21" }
MATCH (j:Judge)-[:PRESIDED]->(h:Hearing)<-[:HAS_HEARING]-(c:Case)-[:INVOKES]->(s:Statute)
WHERE s.act_name = $statute_act AND s.section_no = $section_no
RETURN j.judge_id AS Judge_ID,
       j.judge_name AS Judge_Name,
       j.designation AS Designation,
       count(DISTINCT c) AS Cases_Heard,
       collect(DISTINCT c.case_no) AS Case_Numbers
ORDER BY Cases_Heard DESC;

// -----------------------------------------------------------------------------
// QUERY 8: Lawyers Representing Parties in Cases Citing a Landmark Case
// Multi-hop pattern: (Lawyer)-[:REPRESENTS]->(Party)-[:PARTY_TO]->(Case)-[:CITES]->(Landmark)
// Demonstrates: Ternary relationship navigation connected to precedent graph.
// -----------------------------------------------------------------------------
// Parameters: { "landmark_case": "SC-1978-02" } // Maneka Gandhi
MATCH (l:Lawyer)-[rep:REPRESENTS]->(p:Party)-[:PARTY_TO]->(c:Case)-[:CITES]->(landmark:Case {case_no: $landmark_case})
RETURN DISTINCT l.lawyer_name AS Lawyer_Name,
       l.bar_reg_no AS Bar_Reg_No,
       l.specialization AS Specialization,
       p.party_name AS Client_Party,
       rep.role AS Representation_Role,
       c.case_no AS Case_Invoking_Landmark,
       c.title AS Case_Title;

// -----------------------------------------------------------------------------
// QUERY 9: Citation Depth & Identification of Isolated Cases (Leaves & Islands)
// Evaluates max citation chain depth and detects cases with 0 outgoing or incoming links.
// Demonstrates: Graph topology analysis and pattern comprehensions.
// -----------------------------------------------------------------------------
MATCH (c:Case)
OPTIONAL MATCH path = (c)-[:CITES*1..5]->(ancestor:Case)
WITH c, max(coalesce(length(path), 0)) AS Max_Precedent_Depth
OPTIONAL MATCH (c)<-[:CITES]-(citing:Case)
WITH c, Max_Precedent_Depth, count(citing) AS In_Degree
RETURN c.case_no AS Case_No,
       c.title AS Case_Title,
       Max_Precedent_Depth AS Max_Citation_Depth,
       In_Degree AS Downstream_Citations,
       CASE
         WHEN Max_Precedent_Depth = 0 AND In_Degree = 0 THEN 'Isolated Node (No Citations)'
         WHEN Max_Precedent_Depth = 0 THEN 'Root Precedent (Cites None)'
         WHEN In_Degree = 0 THEN 'Terminal Leaf (Uncited By Later Cases)'
         ELSE 'Active Intermediate Bridge'
       END AS Graph_Classification
ORDER BY In_Degree DESC, Max_Precedent_Depth DESC;

// -----------------------------------------------------------------------------
// QUERY 10: Cross-Court Jurisprudential Influence (High/District -> Supreme)
// Analyzes how regional High Courts and District Courts borrow precedents from
// the Supreme Court of India.
// Demonstrates: Graph traversal bridging organizational hierarchy with precedent chains.
// -----------------------------------------------------------------------------
MATCH (subordinate_case:Case)-[:FILED_IN]->(sub_court:Court)
WHERE sub_court.court_level IN ['High', 'District']
MATCH (subordinate_case)-[:CITES]->(supreme_case:Case)-[:FILED_IN]->(sc:Court {court_level: 'Supreme'})
RETURN sub_court.court_level AS Originating_Court_Level,
       sub_court.court_name AS Originating_Court,
       count(DISTINCT subordinate_case) AS Cases_Citing_Supreme_Court,
       collect(DISTINCT supreme_case.case_no) AS Supreme_Court_Precedents_Cited
ORDER BY Originating_Court_Level ASC, Cases_Citing_Supreme_Court DESC;
