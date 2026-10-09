package kr.sesac.wordcounter.parser;

import java.io.IOException;
import java.util.List;


public class HeaderColumns {

    public static int[] indexesOf(List<String> header, String[] columns) throws IOException {
        int[] indexes = new int[columns.length];

        for (int i = 0; i < columns.length; i++) {
            int found = -1;
            for (int j = 0; j < header.size(); j++) {
                // 헤더 이름은 앞뒤 공백을 제거하고 대소문자를 구분해 비교합니다.
                if (header.get(j).trim().equals(columns[i])) {
                    found = j;
                    break;
                }
            }
            if (found == -1) {
                throw new IOException("분석 열이 헤더에 없습니다: " + columns[i]);
            }
            indexes[i] = found;
        }
        return indexes;
    }
}