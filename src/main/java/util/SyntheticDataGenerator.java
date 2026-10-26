package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * SyntheticDataGenerator - Generates a large synthetic dataset (e.g. 10,000 cases, 50,000 citations)
 * in both Oracle-compatible CSV/SQL and Neo4j-compatible CSV formats.
 * Essential for benchmarking variable-length precedent traversal in Relational SQL vs Neo4j Cypher.
 *
 * Guarantees:
 * 1. Cases are strictly chronological.
 * 2. Citations strictly point backwards in time (newer case cites older case).
 * 3. Power-law distribution: A small set of landmark cases receive high in-degree citations.
 */
public class SyntheticDataGenerator {

    public static class GenerationResult {
        public int totalCases;
        public int totalCitations;
        public String outputDir;
        public long durationMs;

        @Override
        public String toString() {
            return String.format(
                "Synthetic Dataset Generated:\n" +
                "  - Cases: %,d\n" +
                "  - Citations: %,d\n" +
                "  - Output Folder: %s\n" +
                "  - Time Elapsed: %,d ms",
                totalCases, totalCitations, outputDir, durationMs
            );
        }
    }

    public static GenerationResult generateDataset(File outputDir, int numCases, int numCitations) throws IOException {
        long start = System.currentTimeMillis();
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File neoCasesFile = new File(outputDir, "synthetic_cases_neo4j.csv");
        File neoCitationsFile = new File(outputDir, "synthetic_citations_neo4j.csv");
        File oracleCasesCsv = new File(outputDir, "synthetic_cases_oracle.csv");
        File oracleCitationsCsv = new File(outputDir, "synthetic_citations_oracle.csv");
        File oracleSqlFile = new File(outputDir, "synthetic_insert_oracle.sql");

        Random rand = new Random(42); // deterministic seed for reproducibility
        String[] types = {"Constitutional Law", "Criminal Law", "Civil Rights", "Cyber Law", "Commercial Disputes", "Taxation", "Environmental Law"};
        String[] statuses = {"Closed", "Open", "Under Appeal"};
        LocalDate baseDate = LocalDate.of(1950, 1, 1);

        List<String> caseNumbers = new ArrayList<>(numCases);
        List<LocalDate> filingDates = new ArrayList<>(numCases);

        // 1. Generate Cases
        try (BufferedWriter bwNeoCases = new BufferedWriter(new FileWriter(neoCasesFile));
             BufferedWriter bwOracleCases = new BufferedWriter(new FileWriter(oracleCasesCsv));
             BufferedWriter bwSql = new BufferedWriter(new FileWriter(oracleSqlFile))) {

            bwNeoCases.write("case_no,title,filing_date,case_type,status,court_id\n");
            bwOracleCases.write("Case_No,Case_Title,Filing_Date,Case_Type,Status,Court_ID\n");
            bwSql.write("-- Synthetic Cases Bulk Insert for Oracle\n");

            for (int i = 1; i <= numCases; i++) {
                String caseNo = String.format("SYN-CASE-%06d", i);
                caseNumbers.add(caseNo);

                // Advance date progressively so i cites j < i naturally
                long daysAdvance = (long) ((double) i / numCases * 26000.0); // spread across ~70 years
                LocalDate fDate = baseDate.plusDays(daysAdvance);
                filingDates.add(fDate);

                String type = types[rand.nextInt(types.length)];
                String status = statuses[rand.nextInt(statuses.length)];
                int courtId = (rand.nextInt(8)) + 1;
                String title = "Litigant_" + i + " v. Respondent_" + i;

                // Neo4j CSV row
                bwNeoCases.write(String.format("%s,%s,%s,%s,%s,%d\n", caseNo, title, fDate, type, status, courtId));

                // Oracle CSV row
                bwOracleCases.write(String.format("%s,%s,%s,%s,%s,%d\n", caseNo, title, fDate, type, status, courtId));

                // Sample batch of SQL inserts (first 500 rows for quick testing)
                if (i <= 500) {
                    bwSql.write(String.format(
                        "INSERT INTO COURT_CASE (Case_No, Case_Title, Filing_Date, Case_Type, Status, Court_ID) " +
                        "VALUES ('%s', '%s', TO_DATE('%s','YYYY-MM-DD'), '%s', '%s', %d);\n",
                        caseNo, title, fDate, type, status, courtId
                    ));
                }
            }
            if (numCases > 500) {
                bwSql.write("-- [Remaining rows omitted from SQL script; use SQL*Loader or CSV import for bulk scale]\n");
            }
            bwSql.write("COMMIT;\n");
        }

        // 2. Generate Citations (preferential attachment / power-law distribution)
        int citationsCreated = 0;
        Set<String> citationPairs = new HashSet<>();

        try (BufferedWriter bwNeoCit = new BufferedWriter(new FileWriter(neoCitationsFile));
             BufferedWriter bwOracleCit = new BufferedWriter(new FileWriter(oracleCitationsCsv));
             BufferedWriter bwSql = new BufferedWriter(new FileWriter(oracleSqlFile, true))) {

            bwNeoCit.write("citing_case_no,cited_case_no,citation_date\n");
            bwOracleCit.write("Citing_Case_No,Cited_Case_No,Citation_Date\n");
            bwSql.write("\n-- Synthetic Citations Insert for Oracle\n");

            // Define landmark hubs (first 50 cases are mega-precedents cited frequently)
            int hubCount = Math.min(50, numCases / 10);

            while (citationsCreated < numCitations) {
                // Pick citing case from middle/later cases
                int citingIdx = hubCount + rand.nextInt(numCases - hubCount);
                int citedIdx;

                // 60% chance to cite a top landmark hub; 40% chance to cite any strictly prior case
                if (rand.nextDouble() < 0.60) {
                    citedIdx = rand.nextInt(hubCount);
                } else {
                    citedIdx = rand.nextInt(citingIdx);
                }

                if (citingIdx == citedIdx) continue;

                String citingNo = caseNumbers.get(citingIdx);
                String citedNo = caseNumbers.get(citedIdx);
                String key = citingNo + "->" + citedNo;

                if (!citationPairs.contains(key)) {
                    citationPairs.add(key);
                    LocalDate cDate = filingDates.get(citingIdx).plusDays(rand.nextInt(30));

                    bwNeoCit.write(String.format("%s,%s,%s\n", citingNo, citedNo, cDate));
                    bwOracleCit.write(String.format("%s,%s,%s\n", citingNo, citedNo, cDate));

                    if (citationsCreated < 500) {
                        bwSql.write(String.format(
                            "INSERT INTO CITATION (Citing_Case_No, Cited_Case_No, Citation_Date) " +
                            "VALUES ('%s', '%s', TO_DATE('%s','YYYY-MM-DD'));\n",
                            citingNo, citedNo, cDate
                        ));
                    }
                    citationsCreated++;
                }
            }
            bwSql.write("COMMIT;\n");
        }

        GenerationResult res = new GenerationResult();
        res.totalCases = numCases;
        res.totalCitations = citationsCreated;
        res.outputDir = outputDir.getAbsolutePath();
        res.durationMs = System.currentTimeMillis() - start;
        return res;
    }

    public static void main(String[] args) {
        try {
            File out = new File("synthetic_data");
            System.out.println("Generating synthetic dataset (10,000 cases, 50,000 citations)...");
            GenerationResult res = generateDataset(out, 10000, 50000);
            System.out.println(res.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
