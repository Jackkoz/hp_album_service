package com.example.hp.itunes;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ItunesService {

    private static final String SEARCH_URI = "/search";

    private final WebClient itunesWebClient;
    private final ObjectMapper objectMapper;

    /**
     * The API rate limits to ~20 requests per minute (https://performance-partners.apple.com/search-api) but the
     * contents should be changing rarely. An in memory will help us avoid being limited.
     */
    private final ConcurrentHashMap<String, ItunesResponse> cache = new ConcurrentHashMap<>();

    public ItunesService(WebClient itunesWebClient, ObjectMapper objectMapper) {
        this.itunesWebClient = itunesWebClient;
        this.objectMapper = objectMapper;
    }

    public ItunesResponse fetchAlbums(String artist) {
        var encoded = encode(artist);
        var result = cache.computeIfAbsent(encoded, this::makeRequestForAlbum);

        System.out.println(result);
        return result;
    }

    private ItunesResponse makeRequestForAlbum(String encodedArtist) {
        var response = itunesWebClient
                .get()
                .uri(builder ->
                        builder
                                .path(SEARCH_URI)
                                .queryParam("term", encodedArtist)
                                .queryParam("media", "music")
                                .queryParam("entity", "album")
                                .queryParam("version", "2")
                                .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return objectMapper.readValue(response, ItunesResponse.class);
    }

    /**
     * Encodes the search term to be compliant with Apple's requirements:
     * <p>
     * URL encoding replaces spaces with the plus (+) character and all characters except the following are encoded:
     * letters, numbers, periods (.), dashes (-), underscores (_), and asterisks (*).
     * <p>
     * TODO: Ensure remaining characters are encoded.
     */
    private String encode(String request) {
        return request.replace(' ', '+');
    }
}
