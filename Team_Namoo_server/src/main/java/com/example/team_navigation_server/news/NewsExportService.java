package com.example.team_navigation_server.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 수집한 기사 목록을 JSON/CSV 파일로 저장한다.
 * 콜랩에서는 CSV는 pandas.read_csv, JSON은 json.load / pandas.read_json으로 바로 읽을 수 있다.
 */
@Service
public class NewsExportService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${news.export.dir:news-export}")
    private String exportDir;

    public ExportResult export(List<NewsArticle> articles, String baseFileName) throws IOException {
        Path dir = Path.of(exportDir);
        Files.createDirectories(dir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path jsonPath = dir.resolve(baseFileName + "_" + timestamp + ".json");
        Path csvPath = dir.resolve(baseFileName + "_" + timestamp + ".csv");
        Path trainJsonlPath = dir.resolve(baseFileName + "_" + timestamp + "_train.jsonl");

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), articles);
        writeCsv(csvPath, articles);
        writeTrainingJsonl(trainJsonlPath, articles);

        return new ExportResult(
                jsonPath.toAbsolutePath().toString(),
                csvPath.toAbsolutePath().toString(),
                trainJsonlPath.toAbsolutePath().toString(),
                articles.size()
        );
    }

    /**
     * 링크/날짜 등 메타데이터를 뺀 title/content만 담은 jsonl.
     * 콜랩에서 바로 datasets.load_dataset("json", data_files=...) 로 읽어 학습에 쓸 수 있다.
     */
    private void writeTrainingJsonl(Path path, List<NewsArticle> articles) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (NewsArticle a : articles) {
            TrainingRecord record = new TrainingRecord(a.title(), a.content(), a.summary());
            sb.append(objectMapper.writeValueAsString(record)).append('\n');
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private record TrainingRecord(String title, String content, String summary) {
    }

    private void writeCsv(Path csvPath, List<NewsArticle> articles) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("title,originalLink,link,pubDate,description,content,summary\n");
        for (NewsArticle a : articles) {
            sb.append(csvEscape(a.title())).append(',')
                    .append(csvEscape(a.originalLink())).append(',')
                    .append(csvEscape(a.link())).append(',')
                    .append(csvEscape(a.pubDate())).append(',')
                    .append(csvEscape(a.description())).append(',')
                    .append(csvEscape(a.content())).append(',')
                    .append(csvEscape(a.summary())).append('\n');
        }
        Files.writeString(csvPath, sb.toString(), StandardCharsets.UTF_8);
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
        return "\"" + escaped + "\"";
    }

    public record ExportResult(String jsonPath, String csvPath, String trainJsonlPath, int count) {
    }
}
