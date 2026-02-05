package com.example.hp.album;

import com.example.hp.itunes.ItunesService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class AlbumService {

    private final ItunesService itunesService;

    public AlbumService(ItunesService itunesService) {
        this.itunesService = itunesService;
    }

    public List<Album> findAlbums(String artist) {
        var response = itunesService.fetchAlbums(artist);
        var result = new HashMap<String, Album>();
        response.results().stream()
                .map(itunesResult -> new Album(itunesResult.collectionName(), itunesResult.artworkUrl100()))
                .forEach(album -> result.computeIfAbsent(album.name(), name -> album));

        return result.values().stream().toList();
    }
}
