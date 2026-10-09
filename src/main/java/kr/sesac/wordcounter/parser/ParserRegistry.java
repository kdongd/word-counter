package kr.sesac.wordcounter.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ParserRegistry {

    private final Map<String, DocumentParser> parsers = new HashMap<>();
    private final List<String> extensions = new ArrayList<>(); // 등록 순서 유지

    // extension은 점 없이 ("csv")
    public void register(String extension, DocumentParser parser) {
        String key = extension.toLowerCase(Locale.ROOT);
        if (!parsers.containsKey(key)) {
            extensions.add(key);
        }
        parsers.put(key, parser);
    }

    // 지원하지 않는 확장자면 null
    public DocumentParser find(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        return parsers.get(name.substring(dot + 1));
    }

    public boolean isSupported(Path path) {
        return find(path) != null;
    }

    // 예: ".txt .csv .tsv .html .htm"
    public String supportedExtensionsText() {
        StringBuilder builder = new StringBuilder();
        for (String extension : extensions) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append('.').append(extension);
        }
        return builder.toString();
    }
}