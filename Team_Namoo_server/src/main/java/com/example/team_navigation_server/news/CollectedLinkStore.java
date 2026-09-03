package com.example.team_navigation_server.news;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 여러 번 수집을 실행해도 같은 기사가 다시 뽑히지 않도록,
 * 지금까지 수집한 기사의 originalLink를 파일(collected-links.txt)에 누적 기록해두고
 * 다음 실행에서는 이미 있는 링크를 걸러낸다.
 */
@Component
public class CollectedLinkStore {

    @Value("${news.export.dir:news-export}")
    private String exportDir;

    private Set<String> seenLinks;
    private Path storePath;

    private synchronized void ensureLoaded() {
        if (seenLinks != null) {
            return;
        }
        try {
            Path dir = Path.of(exportDir);
            Files.createDirectories(dir);
            storePath = dir.resolve("collected-links.txt");

            seenLinks = ConcurrentHashMap.newKeySet();
            if (Files.exists(storePath)) {
                seenLinks.addAll(Files.readAllLines(storePath, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isCollected(String originalLink) {
        ensureLoaded();
        return seenLinks.contains(originalLink);
    }

    /**
     * 새로 수집한 링크들을 파일에 추가 기록한다. 이미 기록된 링크는 다시 쓰지 않는다.
     */
    public synchronized void markCollected(List<String> originalLinks) {
        ensureLoaded();
        List<String> newOnes = originalLinks.stream()
                .filter(link -> seenLinks.add(link))
                .toList();
        if (newOnes.isEmpty()) {
            return;
        }
        try {
            Files.write(storePath, newOnes, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
