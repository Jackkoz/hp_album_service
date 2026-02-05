package com.example.hp.album;

import com.example.hp.itunes.ItunesResponse;
import com.example.hp.itunes.ItunesResponse.ItunesResult;
import com.example.hp.itunes.ItunesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceTest {

    @Mock
    private ItunesService itunesService;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void filtersRepeatedAlbums() {
        when(itunesService.fetchAlbums("artist")).thenReturn(new ItunesResponse(List.of(
                new ItunesResult("album", "thumbnail"),
                new ItunesResult("album", "different thumbnail"),
                new ItunesResult("album2", "thumbnail2")
        )));

        var albums = albumService.findAlbums("artist");

        assertThat(albums).hasSize(2);
        assertThat(albums.getFirst().name()).isEqualTo("album");
        assertThat(albums.getFirst().thumbnailUrl()).isEqualTo("thumbnail");
        assertThat(albums.getLast().name()).isEqualTo("album2");
    }
}
