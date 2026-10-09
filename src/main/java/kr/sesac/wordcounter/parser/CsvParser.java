package kr.sesac.wordcounter.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CsvParser implements DocumentParser {

    private final String[] columns;

    public CsvParser(String[] columns) {
        this.columns = columns;
    }

    @Override
    public List<String> extractTexts(Path path) throws IOException {
        List<String> texts = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {

            List<String> header = parser.getHeaderNames();
            if (header.isEmpty()) {
                throw new IOException("빈 파일이라 헤더를 읽을 수 없습니다");
            }
            int[] columnIndexes = HeaderColumns.indexesOf(header, columns);

            // 닫히지 않은 따옴표 같은 형식 오류는 이 반복 도중
            // UncheckedIOException으로 던져집니다. (Analyzer에서 잡음)
            for (CSVRecord record : parser) {
                if (record.size() != header.size()) {
                    throw new IOException(record.getRecordNumber() + "번째 데이터 레코드의 셀 수("
                            + record.size() + ")가 헤더(" + header.size() + ")와 다릅니다");
                }
                // 분석 열이 여러 개면 셀마다 따로 넘깁니다. (셀끼리 이어 붙이지 않음)
                for (int index : columnIndexes) {
                    texts.add(record.get(index));
                }
            }
        }
        return texts;
    }
}