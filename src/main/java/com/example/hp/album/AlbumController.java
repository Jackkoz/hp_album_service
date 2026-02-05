package com.example.hp.album;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping(value = "/album/search", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Album>> searchAlbums(@RequestParam String artist) {
        return ResponseEntity.ok(albumService.findAlbums(artist));
    }

}
