package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.MongoService;


import java.util.List;

@Controller
public class ArtistController {

    @Autowired
    private MongoService service;

    @GetMapping("/artists")
    public String artists(Model model){
        if(!model.containsAttribute("artists")) {
            List<Document> artists = service.getArtists();
            model.addAttribute("artists", artists);
        }
        return "artists";
    }

    @GetMapping("/artists/{artistUri}/{albumUri}")
    public String artistsUri(Model model, @PathVariable String artistUri, @PathVariable String albumUri){
        Document album = service.getAlbum(artistUri, albumUri);
        model.addAttribute("album", album);
        return "album";
    }

    @GetMapping("/artists/{uri}")
    public String artistsUri(Model model, @PathVariable String uri){
        Document artist = service.getArtist(uri);
        model.addAttribute("artist", artist);
        return "artist";
    }

    @GetMapping("/artists/search")
    public String artistsSearchGet(){
        return "search";
    }

    @PostMapping("/artists/search")
    public String artistsSearchPost(@RequestParam String search_input){
        return "redirect:/artists/search/"+ search_input;
    }

    @GetMapping("/artists/search/{name}")
    public String artistsSearchResults(Model model, @PathVariable String name){
        List<Document> artists = service.getArtistsByName(name);
        model.addAttribute("artists", artists);
        return "artists";
    }
}
