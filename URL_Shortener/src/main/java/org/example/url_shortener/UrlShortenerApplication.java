package org.example.url_shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class UrlShortenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class UrlShortenerController {
    private final Map<String, UrlEntry> urlStore = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();
    private static final long EXPIRATION_MINUTES = 60;
    private static final String BASE_URL = "http://localhost:8080/api/";

    @PostMapping("/shorten")
    public String shortenUrl(@RequestParam String longUrl) {
        long id = counter.incrementAndGet();
        String shortUrl = toBase62(id);
        urlStore.put(shortUrl, new UrlEntry(longUrl, LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)));
        return BASE_URL + shortUrl;
    }

    @GetMapping("/{shortUrl}")
    public String redirectToLongUrl(@PathVariable String shortUrl) {
        UrlEntry entry = urlStore.get(shortUrl);
        if (entry == null || LocalDateTime.now().isAfter(entry.expiration())) {
            return "URL not found or expired.";
        }
        return "Redirecting to: " + entry.longUrl(); // In a real app: use HTTP 302 redirect
    }

    private String toBase62(long value) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(chars.charAt((int) (value % 62)));
            value /= 62;
        } while (value > 0);
        return sb.reverse().toString();
    }
}

// Java automatically creates getters for record fields
record UrlEntry(String longUrl, LocalDateTime expiration) {}
