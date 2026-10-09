package kr.sesac.wordcounter.analysis;

import kr.sesac.wordcounter.word.WordCounts;

import java.util.List;
import java.util.Map;


public class AnalysisResult {

    private final String input;
    private final int attemptedFiles;
    private final int succeededFiles;
    private final int skippedFiles;
    private final List<String> failures;
    private final WordCounts counts;
    private final List<Map.Entry<String, Long>> sortedEntries;
    private final double elapsedMillis;
    private final String filterDescription;
    private final boolean filterOn;

    public AnalysisResult(String input, int attemptedFiles, int succeededFiles, int skippedFiles,
                          List<String> failures, WordCounts counts, double elapsedMillis,
                          String filterDescription, boolean filterOn) {
        this.input = input;
        this.attemptedFiles = attemptedFiles;
        this.succeededFiles = succeededFiles;
        this.skippedFiles = skippedFiles;
        this.failures = failures;
        this.counts = counts;
        this.sortedEntries = counts.sortedEntries(); // 정렬은 처리 시간 측정이 다 끝난 뒤에 합니다.
        this.elapsedMillis = elapsedMillis;
        this.filterDescription = filterDescription;
        this.filterOn = filterOn;
    }

    public String input() { return input; }
    public int attemptedFiles() { return attemptedFiles; }
    public int succeededFiles() { return succeededFiles; }
    public int failedFiles() { return failures.size(); }
    public int skippedFiles() { return skippedFiles; }
    public List<String> failures() { return failures; }
    public WordCounts counts() { return counts; }
    public List<Map.Entry<String, Long>> sortedEntries() { return sortedEntries; }
    public double elapsedMillis() { return elapsedMillis; }
    public String filterDescription() { return filterDescription; }
    public boolean filterOn() { return filterOn; }

    // 성공한 파일이 하나라도 있어야 단어 조회나 저장이 가능합니다.
    public boolean canQuery() {
        return succeededFiles > 0;
    }
}