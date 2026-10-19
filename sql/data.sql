-- =============================================================================
-- Court Case Management System - Sample Data Script
-- Indian Legal Context with Landmark Cases, Courts, Statutes & Citation Network
-- =============================================================================

-- Clear table contents before populating
DELETE FROM AUDIT_LOG;
DELETE FROM CITATION;
DELETE FROM CASE_STATUTE;
DELETE FROM CASE_PARTY;
DELETE FROM REPRESENTS;
DELETE FROM HEARING;
DELETE FROM STATUTE;
DELETE FROM PARTY;
DELETE FROM LAWYER;
DELETE FROM COURT_CASE;
DELETE FROM JUDGE;
DELETE FROM COURT;

-- 1. COURTS (6 Courts)
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (1, 'Supreme Court of India', 'Supreme', 'New Delhi');
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (2, 'High Court of Judicature at Madras', 'High', 'Chennai, Tamil Nadu');
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (3, 'High Court of Delhi', 'High', 'New Delhi');
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (4, 'High Court of Judicature at Bombay', 'High', 'Mumbai, Maharashtra');
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (5, 'District & Sessions Court, Saket', 'District', 'Saket, New Delhi');
INSERT INTO COURT (Court_ID, Court_Name, Court_Level, Location) VALUES (6, 'District & Sessions Court, Egmore', 'District', 'Chennai, Tamil Nadu');

-- 2. JUDGES (10 Judges)
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (101, 'Hon. Justice A. K. Sikri', 'Chief Justice of India', 1);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (102, 'Hon. Justice D. Y. Chandrachud', 'Senior Puisne Judge', 1);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (103, 'Hon. Justice R. S. Chauhan', 'Chief Justice', 2);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (104, 'Hon. Justice Indira Banerjee', 'Puisne Judge', 2);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (105, 'Hon. Justice S. Muralidhar', 'Chief Justice', 3);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (106, 'Hon. Justice Pradeep Nandrajog', 'Judge', 3);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (107, 'Hon. Justice Nitin Jamdar', 'Senior Judge', 4);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (108, 'Hon. Justice B. P. Dharmadhikari', 'Judge', 4);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (109, 'Hon. Magistrate Vikramjit Sen', 'District Sessions Judge', 5);
INSERT INTO JUDGE (Judge_ID, Judge_Name, Designation, Court_ID) VALUES (110, 'Hon. Magistrate K. Sundaram', 'Chief Metropolitan Magistrate', 6);

-- 3. LANDMARK CASES (20 Cases)
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1950-08', 'A.K. Gopalan v. State of Madras', TO_DATE('1950-03-01', 'YYYY-MM-DD'), 'Constitutional Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1973-01', 'Kesavananda Bharati v. State of Kerala', TO_DATE('1972-08-10', 'YYYY-MM-DD'), 'Constitutional Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1975-10', 'Indira Nehru Gandhi v. Raj Narain', TO_DATE('1975-06-24', 'YYYY-MM-DD'), 'Election Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1978-02', 'Maneka Gandhi v. Union of India', TO_DATE('1977-07-04', 'YYYY-MM-DD'), 'Fundamental Rights', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1980-04', 'Minerva Mills Ltd. v. Union of India', TO_DATE('1979-01-15', 'YYYY-MM-DD'), 'Constitutional Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1980-14', 'Bachan Singh v. State of Punjab', TO_DATE('1979-11-20', 'YYYY-MM-DD'), 'Criminal Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1985-09', 'Mohd. Ahmed Khan v. Shah Bano Begum', TO_DATE('1984-02-12', 'YYYY-MM-DD'), 'Family Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1985-15', 'Olga Tellis v. Bombay Municipal Corp', TO_DATE('1984-09-01', 'YYYY-MM-DD'), 'Civil Rights', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1994-03', 'S.R. Bommai v. Union of India', TO_DATE('1989-10-05', 'YYYY-MM-DD'), 'Administrative Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-1997-03', 'Vishaka v. State of Rajasthan', TO_DATE('1996-05-18', 'YYYY-MM-DD'), 'Human Rights', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2011-11', 'Aruna Shanbaug v. Union of India', TO_DATE('2009-12-16', 'YYYY-MM-DD'), 'Medical Jurisprudence', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2015-06', 'Shreya Singhal v. Union of India', TO_DATE('2013-01-20', 'YYYY-MM-DD'), 'Cyber Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2017-07', 'Shayara Bano v. Union of India', TO_DATE('2015-10-31', 'YYYY-MM-DD'), 'Personal Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2018-05', 'K.S. Puttaswamy v. Union of India', TO_DATE('2012-11-09', 'YYYY-MM-DD'), 'Privacy Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2018-06', 'Navtej Singh Johar v. Union of India', TO_DATE('2016-04-27', 'YYYY-MM-DD'), 'Constitutional Law', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('SC-2018-12', 'Common Cause v. Union of India', TO_DATE('2014-06-11', 'YYYY-MM-DD'), 'Bioethics & Rights', 'Closed', 1);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('HC-2023-101', 'State of TN v. Tech Corp Ltd', TO_DATE('2023-02-14', 'YYYY-MM-DD'), 'Cyber & Contract', 'Open', 2);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('HC-2024-205', 'Delhi Citizens Forum v. MCD', TO_DATE('2024-01-10', 'YYYY-MM-DD'), 'Public Interest Litigation', 'Open', 3);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('DC-2024-501', 'Ramesh Kumar v. Suresh Gupta', TO_DATE('2024-03-22', 'YYYY-MM-DD'), 'Criminal Fraud', 'Under Appeal', 5);
INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) VALUES ('DC-2024-602', 'V. Swaminathan v. Metro Rail Corp', TO_DATE('2024-05-15', 'YYYY-MM-DD'), 'Land Acquisition', 'Open', 6);

-- 4. LAWYERS (10 Lawyers)
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (201, 'Adv. Nani Palkhivala', 'BAR/DEL/1953/001', 'Constitutional Law');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (202, 'Adv. Soli Sorabjee', 'BAR/DEL/1960/045', 'Human Rights');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (203, 'Adv. Fali Nariman', 'BAR/BOM/1965/112', 'Civil Rights');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (204, 'Adv. Ram Jethmalani', 'BAR/DEL/1971/230', 'Criminal Defense');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (205, 'Adv. K. Parasaran', 'BAR/MAS/1975/089', 'Administrative Law');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (206, 'Adv. Pinky Anand', 'BAR/DEL/1985/412', 'Corporate & Cyber');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (207, 'Adv. Gopal Subramanium', 'BAR/DEL/1990/601', 'Public Law');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (208, 'Adv. Mukul Rohatgi', 'BAR/DEL/1995/711', 'Commercial Disputes');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (209, 'Adv. Arvind Datar', 'BAR/MAS/1998/820', 'Taxation & Property');
INSERT INTO LAWYER (Lawyer_ID, Lawyer_Name, Bar_Reg_No, Specialization) VALUES (210, 'Adv. Menaka Guruswamy', 'BAR/DEL/2004/950', 'Constitutional Rights');

-- 5. PARTIES (15 Parties)
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (301, 'Union of India', '+91-11-23381000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (302, 'State of Kerala', '+91-471-2518000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (303, 'State of Tamil Nadu', '+91-44-25671000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (304, 'State of Rajasthan', '+91-141-2227000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (305, 'State of Punjab', '+91-172-2740000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (306, 'State of Madras', '+91-44-25672000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (307, 'Bombay Municipal Corporation', '+91-22-22620000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (308, 'Kesavananda Bharati Sripadagalvaru', '+91-4994-220000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (309, 'Maneka Gandhi', '+91-11-23015000');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (310, 'Shreya Singhal', '+91-9810012345');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (311, 'Dr. K.S. Puttaswamy', '+91-80-22213456');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (312, 'Tech Corp Ltd', '+91-44-43218765');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (313, 'Ramesh Kumar', '+91-9876543210');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (314, 'Suresh Gupta', '+91-9811223344');
INSERT INTO PARTY (Party_ID, Party_Name, Contact_No) VALUES (315, 'V. Swaminathan', '+91-9444011223');

-- 6. STATUTES (10 Statutes)
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (401, 'Constitution of India', 'Article 21', 'Protection of life and personal liberty');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (402, 'Constitution of India', 'Article 14', 'Equality before law');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (403, 'Constitution of India', 'Article 19(1)(a)', 'Freedom of speech and expression');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (404, 'Constitution of India', 'Article 32', 'Remedies for enforcement of fundamental rights');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (405, 'Information Technology Act 2000', 'Section 66A', 'Punishment for sending offensive messages through communication service');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (406, 'Indian Penal Code 1860', 'Section 302', 'Punishment for murder');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (407, 'Indian Penal Code 1860', 'Section 420', 'Cheating and dishonestly inducing delivery of property');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (408, 'Code of Criminal Procedure 1973', 'Section 144', 'Power to issue order in urgent cases of nuisance or apprehended danger');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (409, 'Indian Contract Act 1872', 'Section 73', 'Compensation for loss or damage caused by breach of contract');
INSERT INTO STATUTE (Statute_ID, Act_Name, Section_No, Description) VALUES (410, 'Indian Evidence Act 1872', 'Section 45', 'Opinions of experts');

-- 7. HEARINGS (25 Hearings)
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (501, 'SC-1950-08', 101, TO_DATE('1950-04-15', 'YYYY-MM-DD'), 'Initial arguments on preventive detention');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (502, 'SC-1950-08', 101, TO_DATE('1950-05-19', 'YYYY-MM-DD'), 'Final judgment delivered upholding Article 21 procedure');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (503, 'SC-1973-01', 101, TO_DATE('1972-10-31', 'YYYY-MM-DD'), '13-Judge Constitution bench hearing commenced');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (504, 'SC-1973-01', 101, TO_DATE('1973-03-23', 'YYYY-MM-DD'), 'Arguments concluded on Basic Structure doctrine');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (505, 'SC-1975-10', 102, TO_DATE('1975-07-14', 'YYYY-MM-DD'), 'Emergency powers and election validity reviewed');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (506, 'SC-1978-02', 102, TO_DATE('1977-09-10', 'YYYY-MM-DD'), 'Passport impounding and natural justice principles discussed');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (507, 'SC-1978-02', 102, TO_DATE('1978-01-25', 'YYYY-MM-DD'), 'Expanded scope of Article 21 established');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (508, 'SC-1980-04', 101, TO_DATE('1979-10-22', 'YYYY-MM-DD'), 'Review of 42nd Constitutional Amendment');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (509, 'SC-1980-14', 102, TO_DATE('1980-02-18', 'YYYY-MM-DD'), 'Rarest of rare doctrine for death penalty formulated');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (510, 'SC-1985-09', 101, TO_DATE('1984-11-05', 'YYYY-MM-DD'), 'Maintenance rights under CrPC Section 125 argued');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (511, 'SC-1985-15', 102, TO_DATE('1985-03-12', 'YYYY-MM-DD'), 'Pavement dwellers right to livelihood under Article 21');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (512, 'SC-1994-03', 101, TO_DATE('1993-04-10', 'YYYY-MM-DD'), 'Article 356 misuse and secularism as basic structure');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (513, 'SC-1997-03', 102, TO_DATE('1997-01-20', 'YYYY-MM-DD'), 'Guidelines for prevention of sexual harassment at workplace');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (514, 'SC-2011-11', 102, TO_DATE('2010-08-14', 'YYYY-MM-DD'), 'Passive euthanasia and living wills considered');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (515, 'SC-2015-06', 102, TO_DATE('2014-11-10', 'YYYY-MM-DD'), 'Freedom of speech on internet and IT Act Section 66A');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (516, 'SC-2015-06', 102, TO_DATE('2015-03-24', 'YYYY-MM-DD'), 'Section 66A struck down as unconstitutional');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (517, 'SC-2017-07', 102, TO_DATE('2017-05-18', 'YYYY-MM-DD'), 'Triple Talaq constitutional validity benchmarked');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (518, 'SC-2018-05', 102, TO_DATE('2017-07-26', 'YYYY-MM-DD'), '9-Judge bench hearing on Right to Privacy');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (519, 'SC-2018-05', 102, TO_DATE('2017-08-24', 'YYYY-MM-DD'), 'Right to Privacy declared fundamental right under Art 21');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (520, 'SC-2018-06', 102, TO_DATE('2018-07-17', 'YYYY-MM-DD'), 'Decriminalization of Section 377 IPC');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (521, 'SC-2018-12', 101, TO_DATE('2018-01-16', 'YYYY-MM-DD'), 'Passive euthanasia guidelines finalized');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (522, 'HC-2023-101', 103, TO_DATE('2023-06-10', 'YYYY-MM-DD'), 'Interim injunction sought for cyber breach');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (523, 'HC-2024-205', 105, TO_DATE('2024-03-05', 'YYYY-MM-DD'), 'Notice issued to municipal body regarding drainage');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (524, 'DC-2024-501', 109, TO_DATE('2024-04-18', 'YYYY-MM-DD'), 'Frame of charges under IPC Section 420');
INSERT INTO HEARING (Hearing_ID, Case_No, Judge_ID, Hearing_Date, Remarks) VALUES (525, 'DC-2024-602', 110, TO_DATE('2024-06-02', 'YYYY-MM-DD'), 'Compensation assessment hearing scheduled');

-- 8. CASE_PARTY (Junction: Case & Party)
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1950-08', 306);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1973-01', 308);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1973-01', 302);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1978-02', 309);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1978-02', 301);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1980-04', 301);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1980-14', 305);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1985-15', 307);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-1997-03', 304);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-2015-06', 310);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-2015-06', 301);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-2018-05', 311);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('SC-2018-05', 301);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('HC-2023-101', 303);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('HC-2023-101', 312);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('DC-2024-501', 313);
INSERT INTO CASE_PARTY (Case_No, Party_ID) VALUES ('DC-2024-501', 314);

-- 9. CASE_STATUTE (Junction: Case & Statute)
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1950-08', 401);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1973-01', 404);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1978-02', 401);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1978-02', 402);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1978-02', 403);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1980-04', 404);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1980-14', 406);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1997-03', 401);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-1997-03', 402);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-2015-06', 403);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-2015-06', 405);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('SC-2018-05', 401);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('HC-2023-101', 405);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('HC-2023-101', 409);
INSERT INTO CASE_STATUTE (Case_No, Statute_ID) VALUES ('DC-2024-501', 407);

-- 10. REPRESENTS (Ternary Junction: Lawyer, Party, Case)
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (201, 308, 'SC-1973-01', 'Lead Counsel for Petitioner');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (202, 309, 'SC-1978-02', 'Senior Counsel for Petitioner');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (203, 301, 'SC-1978-02', 'Additional Solicitor General');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (201, 301, 'SC-1980-04', 'Counsel for Petitioner');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (204, 305, 'SC-1980-14', 'Defense Counsel');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (207, 310, 'SC-2015-06', 'Lead Counsel for Petitioner');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (208, 301, 'SC-2015-06', 'Attorney General for India');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (209, 311, 'SC-2018-05', 'Senior Counsel for Petitioner');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (210, 301, 'SC-2018-06', 'Senior Advocate');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (206, 312, 'HC-2023-101', 'Corporate Defense Lawyer');
INSERT INTO REPRESENTS (Lawyer_ID, Party_ID, Case_No, Role) VALUES (205, 303, 'HC-2023-101', 'Advocate General');

-- 11. CITATIONS NETWORK (22 Rows with multi-level chains: Depth >= 4)
-- Chain 1 (Depth 4): SC-2018-05 -> SC-2015-06 -> SC-1978-02 -> SC-1973-01 -> SC-1950-08
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-05', 'SC-2015-06', TO_DATE('2017-08-24', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2015-06', 'SC-1978-02', TO_DATE('2015-03-24', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1978-02', 'SC-1973-01', TO_DATE('1978-01-25', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1973-01', 'SC-1950-08', TO_DATE('1973-04-24', 'YYYY-MM-DD'));

-- Chain 2 (Depth 3): SC-2018-06 -> SC-2018-05 -> SC-1978-02 -> SC-1973-01
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-06', 'SC-2018-05', TO_DATE('2018-09-06', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-05', 'SC-1978-02', TO_DATE('2017-08-24', 'YYYY-MM-DD'));

-- Chain 3 (Depth 3): SC-1980-04 -> SC-1975-10 -> SC-1973-01 -> SC-1950-08
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1980-04', 'SC-1975-10', TO_DATE('1980-05-09', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1975-10', 'SC-1973-01', TO_DATE('1975-11-07', 'YYYY-MM-DD'));

-- Chain 4 (Depth 3): SC-2018-12 -> SC-2011-11 -> SC-1978-02 -> SC-1973-01
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-12', 'SC-2011-11', TO_DATE('2018-03-09', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2011-11', 'SC-1978-02', TO_DATE('2011-03-07', 'YYYY-MM-DD'));

-- Chain 5 (Depth 2): SC-2017-07 -> SC-1985-09 -> SC-1978-02
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2017-07', 'SC-1985-09', TO_DATE('2017-08-22', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1985-09', 'SC-1978-02', TO_DATE('1985-04-23', 'YYYY-MM-DD'));

-- Additional citations enriching the graph topology
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1980-04', 'SC-1973-01', TO_DATE('1980-05-09', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1985-15', 'SC-1978-02', TO_DATE('1985-07-10', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1994-03', 'SC-1973-01', TO_DATE('1994-03-11', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-1997-03', 'SC-1978-02', TO_DATE('1997-08-13', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-05', 'SC-1950-08', TO_DATE('2017-08-24', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-05', 'SC-1973-01', TO_DATE('2017-08-24', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('SC-2018-06', 'SC-1978-02', TO_DATE('2018-09-06', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('HC-2023-101', 'SC-2015-06', TO_DATE('2023-04-01', 'YYYY-MM-DD'));
INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) VALUES ('HC-2024-205', 'SC-1985-15', TO_DATE('2024-02-15', 'YYYY-MM-DD'));

COMMIT;
