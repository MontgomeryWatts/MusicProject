package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.services.DatabaseService;


import java.util.List;

@Controller
@RequestMapping("/artists")
public class ArtistController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String artists(Model model){
        if(!model.containsAttribute("artists")) {
            List<Document> artists = service.getArtists();
            model.addAttribute("artists", artists);
            model.addAttribute("title", "Displays random artists");
        }
        return "artists";
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
