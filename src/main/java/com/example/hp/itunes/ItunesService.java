package com.example.hp.itunes;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ItunesService {

    private static final String SEARCH_URI = "https://itunes.apple.com/search";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * The API rate limits to ~20 requests per minute (https://performance-partners.apple.com/search-api) but the
     * contents should be changing rarely. An in memory will help us avoid being limited.
     */
    private final ConcurrentHashMap<String, ItunesResponse> cache = new ConcurrentHashMap<>();

    public ItunesService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ItunesResponse fetchAlbums(String artist) {
        var encoded = encode(artist);
        var result = cache.computeIfAbsent(encoded, this::makeRequestForAlbum);

        System.out.println(result);
        return result;
    }

    private ItunesResponse makeRequestForAlbum(String encodedArtist) {
        URI uri = UriComponentsBuilder.fromUriString(SEARCH_URI)
                .queryParam("term", encodedArtist)
                .queryParam("media", "music")
                .queryParam("entity", "album")
                .queryParam("version", "2")
                .build()
                .toUri();

        var response = restTemplate.getForEntity(uri, String.class).getBody();
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
