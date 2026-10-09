package kr.sesac.wordcounter.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class TsvParser implements DocumentParser {

    private final String[] columns;

    public TsvParser(String[] columns) {
        this.columns = columns;
    }

    @Override
    public List<String> extractTexts(Path path) throws IOException {
        List<String> texts = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("빈 파일이라 헤더를 읽을 수 없습니다");
            }
            // -1을 넣어야 끝의 빈 셀("a\tb\t")도 버리지 않고 셀 수에 포함됩니다.
            List<String> header = new ArrayList<>();
            for (String cell : headerLine.split("\t", -1)) {
                header.add(cell);
            }
            int[] columnIndexes = HeaderColumns.indexesOf(header, columns);

            String line;
            int recordNumber = 0;
            while ((line = reader.readLine()) != null) {
                // 완전히 빈 줄은 레코드로 보지 않습니다.
                if (line.isEmpty()) {
                    continue;
                }
                recordNumber++;

                String[] cells = line.split("\t", -1);
                if (cells.length != header.size()) {
                    throw new IOException(recordNumber + "번째 데이터 레코드의 셀 수("
                            + cells.length + ")가 헤더(" + header.size() + ")와 다릅니다");
                }
                for (int index : columnIndexes) {
                    texts.add(cells[index]);
                }
            }
        }
        return texts;
    }
}