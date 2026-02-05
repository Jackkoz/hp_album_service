package com.example.hp.itunes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItunesServiceTest {


    @Mock
    private RestTemplate restTemplate;

    private ItunesService itunesService;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @BeforeEach
    void setup() {
        itunesService = new ItunesService(restTemplate, new ObjectMapper());
    }

    @Test
    void fetchesAlbums() throws Exception {
        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(ResponseEntity.ok(ITUNES_RESPONSE));

        var result = itunesService.fetchAlbums("Super Artist");

        assertThat(result.results()).hasSize(2);
        assertThat(result.results().getFirst().collectionName()).isEqualTo("Great album");
        assertThat(result.results().getFirst().artworkUrl100()).isEqualTo("great thumbnail url");
    }

    @Test
    void cachesResults() throws Exception {
        when(restTemplate.getForEntity(any(), eq(String.class))).thenReturn(ResponseEntity.ok(ITUNES_RESPONSE));

        itunesService.fetchAlbums("Great Artist");

        verify(restTemplate).getForEntity(any(), any());
        verifyNoMoreInteractions(restTemplate);

        itunesService.fetchAlbums("Great Artist");
    }

    @Test
    void encodesArtistSearchTerm() throws Exception {
        when(restTemplate.getForEntity(uriCaptor.capture(), eq(String.class))).thenReturn(ResponseEntity.ok(ITUNES_RESPONSE));

        itunesService.fetchAlbums("Great Artist");

        assertThat(uriCaptor.getValue().getQuery()).contains("term=Great+Artist");
    }

    private static final String ITUNES_RESPONSE = """
    {
        "results": [
            {
                "collectionName": "Great album",
                "artworkUrl100": "great thumbnail url"
            },
            {
                "collectionName": "One more album",
                "artworkUrl100": "different thumbnail"
            }
        ]
    }
    """;
}
