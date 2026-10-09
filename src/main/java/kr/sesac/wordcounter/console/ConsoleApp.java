package kr.sesac.wordcounter.console;

import kr.sesac.wordcounter.analysis.AnalysisResult;
import kr.sesac.wordcounter.analysis.Analyzer;
import kr.sesac.wordcounter.output.TsvResultWriter;
import kr.sesac.wordcounter.word.WordCounts;
import kr.sesac.wordcounter.word.WordRules;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class ConsoleApp {

    private static final int DEFAULT_TOP_N = 10;

    private final Scanner scanner;
    private final Analyzer analyzer;
    private final WordRules wordRules;
    private final TsvResultWriter writer;

    private AnalysisResult lastResult = null; // null이면 아직 분석 전

    public ConsoleApp(Scanner scanner, Analyzer analyzer, WordRules wordRules, TsvResultWriter writer) {
        this.scanner = scanner;
        this.analyzer = analyzer;
        this.wordRules = wordRules;
        this.writer = writer;
    }

    public void run() {
        while (true) {
            printMenu();
            String choice = prompt("선택 > ").trim();

            if (choice.equals("1")) {
                startAnalysis();
            } else if (choice.equals("2")) {
                showTopWords();
            } else if (choice.equals("3")) {
                findWord();
            } else if (choice.equals("4")) {
                saveResult();
            } else if (choice.equals("5")) {
                showSummary();
            } else if (choice.equals("0")) {
                System.out.println("프로그램을 종료합니다.");
                return;
            } else {
                System.out.println("0~5 중 하나를 입력하세요.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("문서 단어 분석기");
        System.out.println("1. 새 분석 시작");
        System.out.println("2. 상위 N개 단어 보기");
        System.out.println("3. 특정 단어 횟수 찾기");
        System.out.println("4. 전체 결과 저장");
        System.out.println("5. 최근 분석 요약 보기");
        System.out.println("0. 종료");
    }


    private String prompt(String message) {
        System.out.print(message);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("입력이 끝나 프로그램을 종료합니다.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    // 1. 새 분석

    private void startAnalysis() {
        String inputText;
        Analyzer.Targets targets;

        // 잘못된 경로면 안내하고 다시 입력
        while (true) {
            inputText = prompt("파일 또는 폴더 경로 (취소: 빈 입력) > ").trim();
            if (inputText.isEmpty()) {
                System.out.println("분석을 취소했습니다.");
                return;
            }
            try {
                targets = analyzer.findTargets(inputText);
                break;
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            analyzer.loadStopwords();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return; // 이전 결과 유지
        }

        lastResult = analyzer.analyze(inputText, targets);

        for (String failure : lastResult.failures()) {
            System.out.println("분석 실패: " + failure);
        }
        System.out.println();
        System.out.println("분석 완료");
        printSummary();
    }

    // 2. 상위 N개

    private void showTopWords() {
        if (!checkResultUsable()) {
            return;
        }

        int n;
        while (true) {
            String text = prompt("몇 개를 볼까요? (기본 " + DEFAULT_TOP_N + ") > ").trim();
            if (text.isEmpty()) {
                n = DEFAULT_TOP_N;
                break;
            }
            try {
                n = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("1 이상의 정수를 입력하세요.");
                continue;
            }
            if (n < 1) {
                System.out.println("1 이상의 정수를 입력하세요.");
                continue;
            }
            break;
        }

        List<Map.Entry<String, Long>> entries = lastResult.sortedEntries();
        if (entries.isEmpty()) {
            System.out.println("집계된 단어가 없습니다.");
            return;
        }

        int limit = Math.min(n, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> entry = entries.get(i);
            System.out.println((i + 1) + ". " + entry.getKey() + " : " + entry.getValue() + "회");
        }
    }

    // 3. 특정 단어

    private void findWord() {
        if (!checkResultUsable()) {
            return;
        }

        while (true) {
            String text = prompt("찾을 단어 > ");
            // 조회 문자열에도 같은 단어 규칙을 적용합니다. (JAVA! → java)
            List<String> words = wordRules.tokenize(text);
            if (words.size() != 1) {
                System.out.println("단어 하나를 입력하세요.");
                continue;
            }
            String word = words.get(0);
            System.out.println(word + " : " + lastResult.counts().countOf(word) + "회");
            return;
        }
    }

    // 4. 저장

    private void saveResult() {
        if (!checkResultUsable()) {
            return;
        }
        try {
            writer.write(lastResult.sortedEntries());
            System.out.println("전체 결과 " + lastResult.sortedEntries().size() + "개 단어를 "
                    + writer.outputPath() + "에 저장했습니다.");
        } catch (IOException e) {
            // 저장에 실패해도 lastResult는 그대로라 계속 조회할 수 있습니다.
            System.out.println("저장 실패: " + writer.outputPath() + " (" + Analyzer.describeError(e) + ")");
        }
    }

    // 5. 요약

    private void showSummary() {
        if (lastResult == null) {
            System.out.println("먼저 1번으로 분석을 실행하세요.");
            return;
        }
        printSummary();
    }

    private void printSummary() {
        AnalysisResult r = lastResult;
        WordCounts counts = r.counts();

        System.out.println("입력: " + r.input());
        System.out.println("파일: 시도 " + r.attemptedFiles() + "개 / 성공 " + r.succeededFiles() + "개 / 실패 "
                + r.failedFiles() + "개 / 지원하지 않아 건너뜀 " + r.skippedFiles() + "개");
        for (String failure : r.failures()) {
            System.out.println("  실패: " + failure);
        }
        System.out.println("전체 단어: " + counts.totalWords() + "개 / 서로 다른 단어: " + counts.distinctWords() + "개");
        System.out.println("필터: " + r.filterDescription());
        if (r.filterOn()) {
            long before = counts.totalWords() + counts.excludedByStopwords() + counts.excludedByLength();
            System.out.println("필터로 제외한 단어: 불용어 " + counts.excludedByStopwords() + "개 / 짧은 단어 "
                    + counts.excludedByLength() + "개 (필터 전 전체 " + before + "개)");
        }
        System.out.println("처리 시간: " + String.format("%.1f", r.elapsedMillis()) + "ms");
        if (!r.canQuery()) {
            System.out.println("성공한 파일이 없어 단어 조회와 저장은 할 수 없습니다.");
        }
    }

    // 조회,저장이 가능한 상태인지 확인
    private boolean checkResultUsable() {
        if (lastResult == null) {
            System.out.println("먼저 1번으로 분석을 실행하세요.");
            return false;
        }
        if (!lastResult.canQuery()) {
            System.out.println("최근 분석에서 성공한 파일이 없어 조회·저장할 결과가 없습니다.");
            return false;
        }
        return true;
    }
}