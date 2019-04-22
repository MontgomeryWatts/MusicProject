package com.spotifydb.ui.controllers;

import com.spotifydb.model.Preview;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.spotifydb.application.DatabaseService;


import java.util.List;

@Controller
@RequestMapping("/artists")
public class ArtistController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String artists(Model model){
        List<Preview> artists = service.getArtistsByRandom();
        model.addAttribute("results", artists);
        model.addAttribute("title", "Displays random artists");
        return "results";
    }

    @GetMapping("/{uri}")
    public String artistsUri(Model model, @PathVariable String uri){
        Document artist = service.getArtist(uri);
        model.addAttribute("artist", artist);
        return "artist";
    }

    @GetMapping("/random")
    public String getRandom(){
        String artistUri = service.getRandomArtistURI();
        return "redirect:/artists/" + artistUri;
    }
}
