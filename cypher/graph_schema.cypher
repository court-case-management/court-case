// =============================================================================
// Court Case Management System with Graph-Based Precedent Analysis (DA3)
// Neo4j 5.x Graph Database Schema Definition
// =============================================================================
//
// -----------------------------------------------------------------------------
// RELATIONAL TO GRAPH MODEL MAPPING DECISIONS & JUSTIFICATION
// -----------------------------------------------------------------------------
// In Phase 2 (Oracle RDBMS), relationships were represented via foreign keys
// and junction tables. In Phase 3 (Neo4j Property Graph), entities become Nodes
// and associations become direct, first-class directed Relationships with properties.
//
// +-------------------------------+-----------------------------------+---------------------------------------------------+
// | Relational Concept (Oracle)   | Graph Concept (Neo4j)             | Justification & Elimination Decision              |
// +-------------------------------+-----------------------------------+---------------------------------------------------+
// | COURT table                   | (:Court) Node                     | Independent judicial institution entity.          |
// | JUDGE table                   | (:Judge) Node                     | Judicial officer entity.                          |
// | COURT_CASE table              | (:Case) Node                      | Central legal record entity.                      |
// | LAWYER table                  | (:Lawyer) Node                    | Advocate entity.                                  |
// | PARTY table                   | (:Party) Node                     | Litigant (petitioner/respondent/accused).         |
// | STATUTE table                 | (:Statute) Node                   | Legal acts and sections.                          |
// | HEARING table                 | (:Hearing) Node                   | Kept as node so it can preserve date, remarks,     |
// |                               |                                   | and link Case and Judge simultaneously.           |
// | CASE_PARTY junction table     | [:PARTY_TO] Relationship          | DISAPPEARS: Direct edge (Party)-[:PARTY_TO]->(Case)|
// | CASE_STATUTE junction table   | [:INVOKES] Relationship           | DISAPPEARS: Direct edge (Case)-[:INVOKES]->(Statute)|
// | CITATION recursive junction   | [:CITES {citation_date}]          | DISAPPEARS: Replaced by native graph edges.       |
// |                               | Relationship (Citing -> Cited)    | Crucial for precedent chains without self-joins.  |
// | REPRESENTS ternary junction   | [:REPRESENTS {role, case_no}]     | DISAPPEARS: Resolves ternary relation directly:   |
// |                               |                                   | (Lawyer)-[:REPRESENTS {role, case_no}]->(Party).  |
// | Foreign Key JUDGE.Court_ID    | [:SITS_IN] Relationship           | (Judge)-[:SITS_IN]->(Court)                       |
// | Foreign Key COURT_CASE.Court_ID| [:FILED_IN] Relationship         | (Case)-[:FILED_IN]->(Court)                       |
// +-------------------------------+-----------------------------------+---------------------------------------------------+
//
// WHY JUNCTION TABLES DISAPPEAR:
// 1. In RDBMS, junction tables (CASE_PARTY, CASE_STATUTE, CITATION, REPRESENTS) exist
//    solely to bypass the limitation that relational tables cannot store multi-valued
//    pointers directly. Each traversal requires costly index lookups and join operations.
// 2. In Neo4j, relationships are stored as direct double-linked pointer chains
//    (Index-Free Adjacency). Traversing a citation or representation requires O(1)
//    pointer dereferencing, eliminating join overhead completely.
// 3. Properties on relationships (like citation_date, role, and case_no) allow
//    attributes to live on the connection itself rather than needing a bridge table.
// =============================================================================

// -----------------------------------------------------------------------------
// 1. UNIQUENESS CONSTRAINTS (Neo4j 5.x Syntax: REQUIRE ... IS UNIQUE)
// -----------------------------------------------------------------------------

// Case identifier constraint
CREATE CONSTRAINT constraint_case_no IF NOT EXISTS
FOR (c:Case)
REQUIRE c.case_no IS UNIQUE;

// Court identifier constraint
CREATE CONSTRAINT constraint_court_id IF NOT EXISTS
FOR (co:Court)
REQUIRE co.court_id IS UNIQUE;

// Judge identifier constraint
CREATE CONSTRAINT constraint_judge_id IF NOT EXISTS
FOR (j:Judge)
REQUIRE j.judge_id IS UNIQUE;

// Lawyer identifier and Bar Registration constraints
CREATE CONSTRAINT constraint_lawyer_id IF NOT EXISTS
FOR (l:Lawyer)
REQUIRE l.lawyer_id IS UNIQUE;

CREATE CONSTRAINT constraint_bar_reg_no IF NOT EXISTS
FOR (l:Lawyer)
REQUIRE l.bar_reg_no IS UNIQUE;

// Litigant Party identifier constraint
CREATE CONSTRAINT constraint_party_id IF NOT EXISTS
FOR (p:Party)
REQUIRE p.party_id IS UNIQUE;

// Statute identifier constraint
CREATE CONSTRAINT constraint_statute_id IF NOT EXISTS
FOR (s:Statute)
REQUIRE s.statute_id IS UNIQUE;

// Hearing identifier constraint
CREATE CONSTRAINT constraint_hearing_id IF NOT EXISTS
FOR (h:Hearing)
REQUIRE h.hearing_id IS UNIQUE;

// -----------------------------------------------------------------------------
// 2. PERFORMANCE INDEXES (Lookup & Search Optimization)
// -----------------------------------------------------------------------------

// Index on Case Title for quick title search & auto-complete
CREATE INDEX index_case_title IF NOT EXISTS
FOR (c:Case)
ON (c.title);

// Index on Case Status ('Open', 'Closed', 'Under Appeal') for status filtering
CREATE INDEX index_case_status IF NOT EXISTS
FOR (c:Case)
ON (c.status);

// Index on Case Type for legal classification queries
CREATE INDEX index_case_type IF NOT EXISTS
FOR (c:Case)
ON (c.case_type);

// Index on Court Level ('District', 'High', 'Supreme')
CREATE INDEX index_court_level IF NOT EXISTS
FOR (co:Court)
ON (co.court_level);

// Index on Statute Act Name and Section
CREATE INDEX index_statute_act IF NOT EXISTS
FOR (s:Statute)
ON (s.act_name);

CREATE INDEX index_statute_section IF NOT EXISTS
FOR (s:Statute)
ON (s.section_no);

// Index on Lawyer Name and Specialization
CREATE INDEX index_lawyer_name IF NOT EXISTS
FOR (l:Lawyer)
ON (l.lawyer_name);

CREATE INDEX index_lawyer_spec IF NOT EXISTS
FOR (l:Lawyer)
ON (l.specialization);

// Index on Hearing Date for chronology checks
CREATE INDEX index_hearing_date IF NOT EXISTS
FOR (h:Hearing)
ON (h.hearing_date);

// Index on Citation Relationship Date
CREATE INDEX index_citation_date IF NOT EXISTS
FOR ()-[r:CITES]-()
ON (r.citation_date);
