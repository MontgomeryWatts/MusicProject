package com.spotifydb.ui.controllers;

import com.spotifydb.model.PreviewPage;
import com.spotifydb.model.db.implementations.DatabaseConnection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/artists")
public class ArtistController {

    @Autowired
    private DatabaseConnection db;

    @GetMapping("")
    public String artists(Model model){
        PreviewPage page = db.getArtistsByRandom();
        model.addAttribute("results", page.getPreviews());
        model.addAttribute("title", "Displays random artists");
        return "results";
    }

    @GetMapping("/{id}")
    public String artistsUri(Model model, @PathVariable String id){
        Document artist = db.getArtistById(id);
        model.addAttribute("artist", artist);
        return "artist";
    }

    @GetMapping("/random")
    public String getRandom(){
        String artistId = db.getRandomArtistId();
        return "redirect:/artists/" + artistId;
    }
}
