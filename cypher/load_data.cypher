// =============================================================================
// Court Case Management System - Graph Data Import Script (DA3)
// Neo4j 5.x Cypher Import using LOAD CSV + MERGE + CALL IN TRANSACTIONS
// =============================================================================
// Place CSV files in the Neo4j 'import/' directory (or mounted volume).
// Re-runnable: All queries use MERGE to guarantee idempotency (no duplicate nodes/edges).
// =============================================================================

// -----------------------------------------------------------------------------
// 1. IMPORT COURTS
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///courts.csv' AS row
CALL {
    WITH row
    MERGE (co:Court {court_id: toInteger(row.court_id)})
    ON CREATE SET
        co.court_name = row.court_name,
        co.court_level = row.court_level,
        co.location = row.location
    ON MATCH SET
        co.court_name = row.court_name,
        co.court_level = row.court_level,
        co.location = row.location
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 2. IMPORT JUDGES & [:SITS_IN] RELATIONSHIP
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///judges.csv' AS row
CALL {
    WITH row
    MERGE (j:Judge {judge_id: toInteger(row.judge_id)})
    ON CREATE SET
        j.judge_name = row.judge_name,
        j.designation = row.designation
    ON MATCH SET
        j.judge_name = row.judge_name,
        j.designation = row.designation

    WITH j, row
    WHERE row.court_id IS NOT NULL AND row.court_id <> ''
    MATCH (co:Court {court_id: toInteger(row.court_id)})
    MERGE (j)-[:SITS_IN]->(co)
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 3. IMPORT CASES & [:FILED_IN] RELATIONSHIP
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///cases.csv' AS row
CALL {
    WITH row
    MERGE (c:Case {case_no: row.case_no})
    ON CREATE SET
        c.title = row.title,
        c.filing_date = date(row.filing_date),
        c.case_type = row.case_type,
        c.status = row.status
    ON MATCH SET
        c.title = row.title,
        c.filing_date = date(row.filing_date),
        c.case_type = row.case_type,
        c.status = row.status

    WITH c, row
    WHERE row.court_id IS NOT NULL AND row.court_id <> ''
    MATCH (co:Court {court_id: toInteger(row.court_id)})
    MERGE (c)-[:FILED_IN]->(co)
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 4. IMPORT LAWYERS
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///lawyers.csv' AS row
CALL {
    WITH row
    MERGE (l:Lawyer {lawyer_id: toInteger(row.lawyer_id)})
    ON CREATE SET
        l.lawyer_name = row.lawyer_name,
        l.bar_reg_no = row.bar_reg_no,
        l.specialization = row.specialization
    ON MATCH SET
        l.lawyer_name = row.lawyer_name,
        l.bar_reg_no = row.bar_reg_no,
        l.specialization = row.specialization
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 5. IMPORT PARTIES (LITIGANTS)
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///parties.csv' AS row
CALL {
    WITH row
    MERGE (p:Party {party_id: toInteger(row.party_id)})
    ON CREATE SET
        p.party_name = row.party_name,
        p.contact_no = row.contact_no
    ON MATCH SET
        p.party_name = row.party_name,
        p.contact_no = row.contact_no
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 6. IMPORT STATUTES
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///statutes.csv' AS row
CALL {
    WITH row
    MERGE (s:Statute {statute_id: toInteger(row.statute_id)})
    ON CREATE SET
        s.act_name = row.act_name,
        s.section_no = row.section_no,
        s.description = row.description
    ON MATCH SET
        s.act_name = row.act_name,
        s.section_no = row.section_no,
        s.description = row.description
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 7. IMPORT HEARINGS, [:HAS_HEARING], AND [:PRESIDED] RELATIONSHIPS
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///hearings.csv' AS row
CALL {
    WITH row
    MERGE (h:Hearing {hearing_id: toInteger(row.hearing_id)})
    ON CREATE SET
        h.hearing_date = date(row.hearing_date),
        h.remarks = row.remarks
    ON MATCH SET
        h.hearing_date = date(row.hearing_date),
        h.remarks = row.remarks

    WITH h, row
    MATCH (c:Case {case_no: row.case_no})
    MERGE (c)-[:HAS_HEARING]->(h)

    WITH h, row
    WHERE row.judge_id IS NOT NULL AND row.judge_id <> ''
    MATCH (j:Judge {judge_id: toInteger(row.judge_id)})
    MERGE (j)-[:PRESIDED]->(h)
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 8. IMPORT [:PARTY_TO] RELATIONSHIPS (Resolved from CASE_PARTY)
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///case_parties.csv' AS row
CALL {
    WITH row
    MATCH (p:Party {party_id: toInteger(row.party_id)})
    MATCH (c:Case {case_no: row.case_no})
    MERGE (p)-[:PARTY_TO]->(c)
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 9. IMPORT [:INVOKES] RELATIONSHIPS (Resolved from CASE_STATUTE)
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///case_statutes.csv' AS row
CALL {
    WITH row
    MATCH (c:Case {case_no: row.case_no})
    MATCH (s:Statute {statute_id: toInteger(row.statute_id)})
    MERGE (c)-[:INVOKES]->(s)
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 10. IMPORT [:REPRESENTS] RELATIONSHIPS (Resolving Ternary REPRESENTS)
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///represents.csv' AS row
CALL {
    WITH row
    MATCH (l:Lawyer {lawyer_id: toInteger(row.lawyer_id)})
    MATCH (p:Party {party_id: toInteger(row.party_id)})
    MERGE (l)-[r:REPRESENTS {case_no: row.case_no}]->(p)
    ON CREATE SET r.role = row.role
    ON MATCH SET r.role = row.role
} IN TRANSACTIONS OF 500 ROWS;

// -----------------------------------------------------------------------------
// 11. IMPORT CITATIONS [:CITES] RELATIONSHIPS (Citing -> Cited Precedent)
// -----------------------------------------------------------------------------
LOAD CSV WITH HEADERS FROM 'file:///citations.csv' AS row
CALL {
    WITH row
    MATCH (c1:Case {case_no: row.citing_case_no})
    MATCH (c2:Case {case_no: row.cited_case_no})
    MERGE (c1)-[r:CITES]->(c2)
    ON CREATE SET r.citation_date = date(row.citation_date)
    ON MATCH SET r.citation_date = date(row.citation_date)
} IN TRANSACTIONS OF 500 ROWS;
