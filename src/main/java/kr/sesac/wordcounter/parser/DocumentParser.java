package kr.sesac.wordcounter.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public interface DocumentParser {
    List<String> extractTexts(Path path) throws IOException;
}