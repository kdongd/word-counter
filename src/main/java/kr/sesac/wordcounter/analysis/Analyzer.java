package kr.sesac.wordcounter.analysis;

import kr.sesac.wordcounter.parser.DocumentParser;
import kr.sesac.wordcounter.parser.ParserRegistry;
import kr.sesac.wordcounter.word.WordCounts;
import kr.sesac.wordcounter.word.WordRules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


// 분석 : 입력 경로에서 파일 목록을 만들고 파일마다 파서를 골라 집계한 뒤에 성공한 결과만 합산하고 시간을 잽니다.
public class Analyzer {

    // 입력 경로를 확인한 결과 : 분석할 파일 목록과 지원하지 않아 건너뛴 수
    public static class Targets {
        final List<Path> files = new ArrayList<>();
        int skipped = 0;
    }

    private final ParserRegistry registry;
    private final WordRules wordRules;
    private final boolean includeSubfolders;
    private final boolean useStopwords;
    private final Path stopwordsPath;

    public Analyzer(ParserRegistry registry, WordRules wordRules, boolean includeSubfolders,
                    boolean useStopwords, Path stopwordsPath) {
        this.registry = registry;
        this.wordRules = wordRules;
        this.includeSubfolders = includeSubfolders;
        this.useStopwords = useStopwords;
        this.stopwordsPath = stopwordsPath;
    }

    // 1. 입력 경로 → 파일 목록

    public Targets findTargets(String inputText) throws IOException {
        Path path;
        try {
            path = Path.of(inputText);
        } catch (InvalidPathException e) {
            throw new IOException("올바른 경로 형식이 아닙니다: " + inputText);
        }
        if (!Files.exists(path)) {
            throw new IOException("경로를 찾을 수 없습니다: " + inputText);
        }
        if (!Files.isReadable(path)) {
            throw new IOException("경로를 읽을 수 없습니다(권한 없음): " + inputText);
        }

        Targets targets = new Targets();
        if (Files.isRegularFile(path)) {
            if (!registry.isSupported(path)) {
                throw new IOException("지원하지 않는 파일입니다. 지원 확장자: " + registry.supportedExtensionsText());
            }
            targets.files.add(path);
            return targets;
        }
        if (Files.isDirectory(path)) {
            try {
                collectFiles(path, targets);
            } catch (IOException | UncheckedIOException e) {
                throw new IOException("폴더를 읽을 수 없습니다: " + inputText + " (" + describeError(e) + ")");
            }
            if (targets.files.isEmpty()) {
                throw new IOException("폴더에 지원하는 파일이 없습니다 (지원하지 않아 건너뜀 " + targets.skipped
                        + "개). 지원 확장자: " + registry.supportedExtensionsText());
            }
            return targets;
        }
        throw new IOException("일반 파일이나 폴더가 아닙니다: " + inputText);
    }


    // 폴더 안의 파일을 이름 순서로 모음. includeSubfolders가 true면 하위 폴더도 같은 규칙으로 적용됩니다.

    private void collectFiles(Path directory, Targets targets) throws IOException {
        List<Path> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path child : stream) {
                children.add(child);
            }
        }
        Collections.sort(children);

        for (Path child : children) {
            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                if (includeSubfolders) {
                    collectFiles(child, targets);
                }
                continue;
            }
            if (!Files.isRegularFile(child)) {
                continue;
            }
            if (registry.isSupported(child)) {
                targets.files.add(child);
            } else {
                targets.skipped++;
            }
        }
    }

    // 2. 불용어 읽기

    // 불용어를 쓰도록 설정 되어있으면 파일을 읽고 단어 규칙에 적용합니다.
    // 각 줄마다 규칙을 적용하기 때문에 "Java!" 라고 적어도 java로 등록됩니다. 빈줄이나 # 같은 단어로 시작하는 줄은 무시합니다.
    public void loadStopwords() throws IOException {
        Set<String> words = new HashSet<>();
        if (useStopwords) {
            try (BufferedReader reader = Files.newBufferedReader(stopwordsPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    words.addAll(wordRules.tokenize(trimmed));
                }
            } catch (IOException | UncheckedIOException e) {
                throw new IOException("불용어 파일을 읽을 수 없어 분석을 시작하지 않습니다: " + stopwordsPath
                        + " (" + describeError(e) + ")");
            }
        }
        wordRules.setStopwords(words);
    }

    // 3. 분석

    public AnalysisResult analyze(String inputText, Targets targets) {
        WordCounts total = new WordCounts();
        int succeeded = 0;
        List<String> failures = new ArrayList<>();

        // 시간 측정: 첫 파일 읽기 직전 ~ 전체 집계 완료
        long start = System.nanoTime();

        for (Path file : targets.files) {
            try {
                // 파일 하나를 끝까지 성공했을 때만 전체에 합칩니다.
                WordCounts fileCounts = countFile(file);
                total.addAll(fileCounts);
                succeeded++;
            } catch (IOException | RuntimeException e) {
                // RuntimeException: Commons CSV의 UncheckedIOException 등 읽는 도중의 오류 말고 그외에 다른 오류
                failures.add(file + " (" + describeError(e) + ")");
            }
        }

        long end = System.nanoTime();
        double elapsedMillis = (end - start) / 1_000_000.0;

        return new AnalysisResult(inputText, targets.files.size(), succeeded, targets.skipped,
                failures, total, elapsedMillis, describeFilters(), isFilterOn());
    }

    // 파일 하나를 처리해 그 파일만의 집계를 돌려줍니다.
    private WordCounts countFile(Path file) throws IOException {
        DocumentParser parser = registry.find(file);
        if (parser == null) {
            throw new IOException("지원하지 않는 확장자입니다");
        }

        WordCounts counts = new WordCounts();
        for (String text : parser.extractTexts(file)) {
            for (String word : wordRules.tokenize(text)) {
                if (wordRules.isStopword(word)) {
                    counts.excludeStopword();
                } else if (wordRules.isTooShort(word)) {
                    counts.excludeShortWord();
                } else {
                    counts.add(word);
                }
            }
        }
        return counts;
    }

    private boolean isFilterOn() {
        return useStopwords || wordRules.minWordLength() > 1;
    }

    private String describeFilters() {
        String stop = useStopwords ? "불용어 " + wordRules.stopwordCount() + "개(" + stopwordsPath + ")" : "불용어 끔";
        String length = wordRules.minWordLength() > 1 ? "최소 길이 " + wordRules.minWordLength() + "글자" : "최소 길이 끔";
        String folder = includeSubfolders ? "하위 폴더 포함" : "하위 폴더 제외";
        return stop + " / " + length + " / " + folder;
    }

    // 사용자에게 보여줄 실패 이유
    public static String describeError(Exception e) {
        Throwable cause = e;
        if (e instanceof UncheckedIOException && e.getCause() != null) {
            cause = e.getCause();
        }
        if (cause instanceof MalformedInputException) {
            return "UTF-8로 읽을 수 없는 파일입니다";
        }
        if (cause instanceof AccessDeniedException) {
            return "읽기 권한이 없습니다";
        }
        if (cause instanceof NoSuchFileException) {
            return "파일이 없습니다";
        }
        if (cause.getMessage() == null) {
            return cause.getClass().getSimpleName();
        }
        return cause.getMessage();
    }
}