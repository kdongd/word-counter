package kr.sesac.wordcounter.word;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 단어별 집계 : 단어를 세고, 다른 집계를 합치고, 정렬합니다.
// 필터로 제외한 수도 같이 셉니다.

public class WordCounts {

    private final Map<String, Long> counts = new HashMap<>();
    private long totalWords = 0;
    private long excludedByStopwords = 0;
    private long excludedByLength = 0;

    public void add(String word) {
        addCount(word, 1);
    }

    public void excludeStopword() {
        excludedByStopwords++;
    }

    public void excludeShortWord() {
        excludedByLength++;
    }

    // 다른 집계(파일 하나의 결과)를 이 집계에 더합니다.
    public void addAll(WordCounts other) {
        for (Map.Entry<String, Long> entry : other.counts.entrySet()) {
            addCount(entry.getKey(), entry.getValue());
        }
        excludedByStopwords += other.excludedByStopwords;
        excludedByLength += other.excludedByLength;
    }

    private void addCount(String word, long count) {
        if (counts.containsKey(word)) {
            counts.put(word, counts.get(word) + count);
        } else {
            counts.put(word, count);
        }
        totalWords += count;
    }

    public long countOf(String word) {
        if (counts.containsKey(word)) {
            return counts.get(word);
        }
        return 0;
    }

    public long totalWords() {
        return totalWords;
    }

    public int distinctWords() {
        return counts.size();
    }

    public long excludedByStopwords() {
        return excludedByStopwords;
    }

    public long excludedByLength() {
        return excludedByLength;
    }

    // 횟수 내림차순, 같으면 String.compareTo 오름차순
    public List<Map.Entry<String, Long>> sortedEntries() {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
                int byCount = Long.compare(b.getValue(), a.getValue());
                if (byCount != 0) {
                    return byCount;
                }
                return a.getKey().compareTo(b.getKey());
            }
        });
        return entries;
    }
}