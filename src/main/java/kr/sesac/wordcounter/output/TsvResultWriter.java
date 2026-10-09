package kr.sesac.wordcounter.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;


public class TsvResultWriter {

    private final Path outputPath;

    public TsvResultWriter(Path outputPath) {
        this.outputPath = outputPath;
    }

    public Path outputPath() {
        return outputPath;
    }

    public void write(List<Map.Entry<String, Long>> sortedEntries) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("word\tcount");
            writer.newLine();
            for (Map.Entry<String, Long> entry : sortedEntries) {
                writer.write(entry.getKey() + "\t" + entry.getValue());
                writer.newLine();
            }
        }
    }
}