package com.example.urlshortener;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortenerService {
    private final Map<String, ShortenedUrl> urlStorage = new ConcurrentHashMap<>();
    private final Map<String, String> userUrls = new ConcurrentHashMap<>();
    
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    public String shortenUrl(String originalUrl, String userUUID, int maxClicks) {
        String shortUrl = UUID.randomUUID().toString().substring(0, 6);
        Instant expiryTime = Instant.now().plusSeconds(86400); // 24 часа
        ShortenedUrl shortenedUrl = new ShortenedUrl(originalUrl, maxClicks, expiryTime, userUUID);
        urlStorage.put(shortUrl, shortenedUrl);
        userUrls.put(shortUrl, userUUID);
        return shortUrl;
    }
    
    public String getOriginalUrl(String shortUrl) {
        ShortenedUrl shortenedUrl = urlStorage.get(shortUrl);
        if (shortenedUrl == null || shortenedUrl.isExpired()) {
            return "Ссылка недоступна";
        }
        if (!shortenedUrl.decrementClicks()) {
            return "Лимит переходов исчерпан";
        }
        return shortenedUrl.getOriginalUrl();
    }
    
    public boolean deleteUrl(String shortUrl, String userUUID) {
        if (userUrls.containsKey(shortUrl) && userUrls.get(shortUrl).equals(userUUID)) {
            urlStorage.remove(shortUrl);
            userUrls.remove(shortUrl);
            return true;
        }
        return false;
    }
    
    public void openShortenedUrl(String shortUrl) {
        try {
            String originalUrl = getOriginalUrl(shortUrl);
            if (!originalUrl.startsWith("http")) {
                System.out.println(originalUrl);
                return;
            }
            java.awt.Desktop.getDesktop().browse(new URI(originalUrl));
        } catch (Exception e) {
            System.out.println("Ошибка при открытии ссылки");
        }
    }
    
    private static class ShortenedUrl {
        private final String originalUrl;
        private int remainingClicks;
        private final Instant expiryTime;
        private final String ownerUUID;

        public ShortenedUrl(String originalUrl, int maxClicks, Instant expiryTime, String ownerUUID) {
            this.originalUrl = originalUrl;
            this.remainingClicks = maxClicks;
            this.expiryTime = expiryTime;
            this.ownerUUID = ownerUUID;
        }

        public String getOriginalUrl() {
            return originalUrl;
        }

        public boolean decrementClicks() {
            if (remainingClicks > 0) {
                remainingClicks--;
                return true;
            }
            return false;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }
}
