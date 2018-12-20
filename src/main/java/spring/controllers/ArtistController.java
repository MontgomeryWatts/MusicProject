package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.services.MongoService;


import java.util.List;

@Controller
@RequestMapping("/artists")
public class ArtistController {

    @Autowired
    private MongoService service;

    @GetMapping("")
    public String artists(Model model){
        if(!model.containsAttribute("artists")) {
            List<Document> artists = service.getArtists();
            model.addAttribute("artists", artists);
        }
        return "artists";
    }

    @GetMapping("/{artistUri}/{albumUri}")
    public String artistsUri(Model model, @PathVariable String artistUri, @PathVariable String albumUri){
        Document album = service.getAlbum(artistUri, albumUri);
        model.addAttribute("album", album);
        return "album";
    }

    @GetMapping("/{uri}")
    public String artistsUri(Model model, @PathVariable String uri){
        Document artist = service.getArtist(uri);
        model.addAttribute("artist", artist);
        return "artist";
    }

    @GetMapping("/search")
    public String artistsSearchGet(){
        return "search";
    }
}
