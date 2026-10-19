-- =============================================================================
-- Court Case Management System - PL/SQL Procedures, Functions, and Triggers
-- Oracle Database 21c Syntax
-- =============================================================================

-- 1. PROCEDURE: add_case
-- Adds a new case into COURT_CASE table with exception handling
CREATE OR REPLACE PROCEDURE add_case (
    p_case_no     IN COURT_CASE.Case_No%TYPE,
    p_title       IN COURT_CASE.Case_Title%TYPE,
    p_filing_date IN COURT_CASE.Filing_Date%TYPE,
    p_type        IN COURT_CASE.Case_Type%TYPE,
    p_status      IN COURT_CASE.Status%TYPE,
    p_court_id    IN COURT_CASE.Court_ID%TYPE
) AS
BEGIN
    INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID)
    VALUES (p_case_no, p_title, p_filing_date, p_type, p_status, p_court_id);
    COMMIT;
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20003, 'Case with Case No (' || p_case_no || ') already exists!');
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20000, 'Error adding case: ' || SQLERRM);
END;
/

-- 2. PROCEDURE: schedule_hearing
-- Schedules a hearing for an existing case
CREATE OR REPLACE PROCEDURE schedule_hearing (
    p_case_no   IN HEARING.Case_No%TYPE,
    p_judge_id  IN HEARING.Judge_ID%TYPE,
    p_date      IN HEARING.Hearing_Date%TYPE,
    p_remarks   IN HEARING.Remarks%TYPE
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM COURT_CASE WHERE Case_No = p_case_no;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Case No (' || p_case_no || ') does not exist!');
    END IF;

    INSERT INTO HEARING (Case_No, Judge_ID, Hearing_Date, Remarks)
    VALUES (p_case_no, p_judge_id, p_date, p_remarks);
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
/

-- 3. PROCEDURE: update_case_status
-- Updates case status (triggers status audit log)
CREATE OR REPLACE PROCEDURE update_case_status (
    p_case_no    IN COURT_CASE.Case_No%TYPE,
    p_new_status IN COURT_CASE.Status%TYPE
) AS
BEGIN
    UPDATE COURT_CASE
    SET Status = p_new_status
    WHERE Case_No = p_case_no;

    IF SQL%NOTFOUND THEN
        RAISE_APPLICATION_ERROR(-20005, 'Case No (' || p_case_no || ') not found!');
    END IF;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
/

-- 4. PROCEDURE: delete_case
-- Safely removes a case and all associated dependent records across tables
CREATE OR REPLACE PROCEDURE delete_case (
    p_case_no IN COURT_CASE.Case_No%TYPE
) AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM COURT_CASE WHERE Case_No = p_case_no;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Case No (' || p_case_no || ') does not exist!');
    END IF;

    -- Clean up child and junction records first
    DELETE FROM REPRESENTS WHERE Case_No = p_case_no;
    DELETE FROM CASE_PARTY WHERE Case_No = p_case_no;
    DELETE FROM CASE_STATUTE WHERE Case_No = p_case_no;
    DELETE FROM CITATION WHERE Citing_Case_No = p_case_no OR Cited_Case_No = p_case_no;
    DELETE FROM HEARING WHERE Case_No = p_case_no;
    DELETE FROM AUDIT_LOG WHERE Case_No = p_case_no;
    DELETE FROM COURT_CASE WHERE Case_No = p_case_no;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
/

-- 5. FUNCTION: get_citation_count
-- Returns the total number of times a case is cited by other cases
CREATE OR REPLACE FUNCTION get_citation_count (
    p_case_no IN COURT_CASE.Case_No%TYPE
) RETURN NUMBER AS
    v_count NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM CITATION
    WHERE Cited_Case_No = p_case_no;
    
    RETURN v_count;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END;
/

-- 6. FUNCTION: get_hearing_count
-- Returns the total number of hearings scheduled for a case
CREATE OR REPLACE FUNCTION get_hearing_count (
    p_case_no IN COURT_CASE.Case_No%TYPE
) RETURN NUMBER AS
    v_count NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM HEARING
    WHERE Case_No = p_case_no;
    
    RETURN v_count;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END;
/

-- 7. TRIGGER: trg_hearing_date_check
-- Prevents scheduling a hearing before the filing date of the case
CREATE OR REPLACE TRIGGER trg_hearing_date_check
BEFORE INSERT OR UPDATE ON HEARING
FOR EACH ROW
DECLARE
    v_filing_date DATE;
BEGIN
    SELECT Filing_Date INTO v_filing_date
    FROM COURT_CASE
    WHERE Case_No = :NEW.Case_No;

    IF :NEW.Hearing_Date < v_filing_date THEN
        RAISE_APPLICATION_ERROR(-20001, 
            'Invalid Hearing Date (' || TO_CHAR(:NEW.Hearing_Date, 'YYYY-MM-DD') || 
            '). Hearing date cannot be before case filing date (' || 
            TO_CHAR(v_filing_date, 'YYYY-MM-DD') || ')!');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20004, 'Case No (' || :NEW.Case_No || ') does not exist!');
END;
/

-- 8. TRIGGER: trg_status_audit
-- Automatically records status changes in AUDIT_LOG table
CREATE OR REPLACE TRIGGER trg_status_audit
AFTER UPDATE OF Status ON COURT_CASE
FOR EACH ROW
WHEN (OLD.Status IS NULL OR OLD.Status <> NEW.Status)
BEGIN
    INSERT INTO AUDIT_LOG (Case_No, Old_Status, New_Status, Changed_On)
    VALUES (:OLD.Case_No, :OLD.Status, :NEW.Status, SYSDATE);
END;
/

-- 9. TRIGGER: trg_no_self_citation
-- Blocks citation entries where citing case is identical to cited case
CREATE OR REPLACE TRIGGER trg_no_self_citation
BEFORE INSERT OR UPDATE ON CITATION
FOR EACH ROW
BEGIN
    IF :NEW.Citing_Case_No = :NEW.Cited_Case_No THEN
        RAISE_APPLICATION_ERROR(-20002, 'Self-Citation Error: Case (' || :NEW.Citing_Case_No || ') cannot cite itself!');
    END IF;
END;
/

-- 10. CURSOR PROCEDURE: get_case_hearings
-- Returns a SYS_REFCURSOR pointing to hearings of a specific case
CREATE OR REPLACE PROCEDURE get_case_hearings (
    p_case_no IN COURT_CASE.Case_No%TYPE,
    p_cursor  OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
        SELECT H.Hearing_ID, H.Case_No, H.Hearing_Date, H.Remarks, J.Judge_Name
        FROM HEARING H
        LEFT JOIN JUDGE J ON H.Judge_ID = J.Judge_ID
        WHERE H.Case_No = p_case_no
        ORDER BY H.Hearing_Date DESC;
END;
/
