package kr.sesac.wordcounter.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class HtmlParser implements DocumentParser {

    private final String selector;
    private final String excludeSelector;

    public HtmlParser(String selector, String excludeSelector) {
        this.selector = selector;
        this.excludeSelector = excludeSelector;
    }

    @Override
    public List<String> extractTexts(Path path) throws IOException {
        // Files.readString은 UTF-8로 읽을 수 없는 파일이면 예외를 던집니다.
        String html = Files.readString(path, StandardCharsets.UTF_8);
        Document document = Jsoup.parse(html);

        Elements found = document.select(selector);
        if (found.size() != 1) {
            throw new IOException("본문 요소(" + selector + ")를 " + found.size()
                    + "개 찾았습니다. 정확히 1개여야 합니다");
        }
        Element content = found.first();

        // 본문 안쪽의 script, style, nav, header, footer를 제거
        content.select(excludeSelector).remove();

        // text(): 태그,주석,속성은 빼고 글자만 돌려줍니다.
        List<String> texts = new ArrayList<>();
        texts.add(content.text());
        return texts;
    }
}