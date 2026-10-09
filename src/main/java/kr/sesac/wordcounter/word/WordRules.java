package kr.sesac.wordcounter.word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

// 단어 처리 규칙 : 텍스트를 단어로 나누고 불용어나 최소 길이로 걸러낼 단어인지 판단합니다.

public class WordRules {

    // 허용 문자(영문, 숫자, 한글 가~힣, 자음 ㄱ~ㅎ, 모음 ㅏ~ㅣ)가 아닌 문자는 모두 경계
    private static final Pattern DELIMITER = Pattern.compile("[^A-Za-z0-9가-힣ㄱ-ㅎㅏ-ㅣ]+");
    private static final Pattern DIGITS_ONLY = Pattern.compile("[0-9]+");

    private final int minWordLength;
    private Set<String> stopwords = new HashSet<>();

    public WordRules(int minWordLength) {
        this.minWordLength = minWordLength;
    }

    // 기본 단어 규칙: 경계로 나누고, 빈 문자열,숫자만 있는 토큰은 버리고 소문자로 바꿉니다.
    public List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        for (String token : DELIMITER.split(text)) {
            if (token.isEmpty()) {
                continue; // 구분자로 시작하면 생기는 빈 문자열
            }
            if (DIGITS_ONLY.matcher(token).matches()) {
                continue; // 숫자만 있는 토큰
            }
            words.add(token.toLowerCase(Locale.ROOT));
        }
        return words;
    }

    public void setStopwords(Set<String> stopwords) {
        this.stopwords = stopwords;
    }

    public int stopwordCount() {
        return stopwords.size();
    }

    public boolean isStopword(String word) {
        return stopwords.contains(word);
    }

    // 글자 수 기준 (한글 한 글자, 영문 한 글자 모두 1)
    public boolean isTooShort(String word) {
        return word.length() < minWordLength;
    }

    public int minWordLength() {
        return minWordLength;
    }
}