package com.example.hp.display;

import com.example.hp.album.AlbumService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AlbumViewController {

    private final AlbumService albumService;

    public AlbumViewController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping(value = "/albums")
    String displayAlbums(Model model, @RequestParam String artist) {
        var response = albumService.findAlbums(artist);
        model.addAttribute("albums", response);
        return "albums";
    }
}
