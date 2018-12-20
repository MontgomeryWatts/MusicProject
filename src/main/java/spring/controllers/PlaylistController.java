package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.MongoService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/playlist")
public class PlaylistController {
    @Autowired
    private MongoService service;

    @GetMapping("")
    public String playlist(){
        return "/html/playlistform.html";
    }

    @PostMapping("")
    public String artistsSearchPost(Model model, @RequestParam String artist_input, @RequestParam String genre_input,
                                    @RequestParam(defaultValue = "1") String hour_input, @RequestParam(defaultValue = "0") String minute_input,
                                    @RequestParam(required = false) String explicit_input){

        Set<String> artists = new HashSet<>(Arrays.asList(artist_input.split(",")));
        Set<String> genres = new HashSet<>(Arrays.asList(genre_input.split(",")));

        boolean allowExplicit = (explicit_input != null);
        int duration = Integer.parseInt(hour_input) * 3600 + Integer.parseInt(minute_input) * 60;

        List<Document> songs = service.createPlaylist(artists, genres, duration, allowExplicit, 2000, 2020);
        model.addAttribute("songs", songs);
        return "createdPlaylist";
    }

}
