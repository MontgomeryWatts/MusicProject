package com.spotifydb.ui.controllers;

import com.spotifydb.model.Preview;
import com.spotifydb.model.db.queries.DatabaseQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.spotifydb.application.DatabaseService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String genres(Model model, @RequestParam(name = "letter", required = false) String letter){
        List<String> genres;

        List<Character> alphabet = new ArrayList<>();
        for(char c = 'A'; c <= 'Z'; c++)
            alphabet.add(c);
        model.addAttribute("alphabet", alphabet);

        if (letter != null){
            char c = letter.charAt(0);
            genres = service.getGenresByLetter(c);
            model.addAttribute("title", "Genres starting with " + c);
        } else{
            genres = service.getAllGenres();
            model.addAttribute("title", "All genres");
        }

        model.addAttribute("genres", genres);
        return "genres";
    }

    @GetMapping("/{genre}")
    public String genre(Model model, @PathVariable String genre, @RequestParam(name = "p", required = false, defaultValue = "1") String page_number){
        int page = 1;
        if (page_number.matches("\\d+"))
            page = Integer.parseInt(page_number);

        int offset = DatabaseQueries.SMALL_SAMPLE_SIZE * (page - 1);
        int limit = DatabaseQueries.SMALL_SAMPLE_SIZE;
        List<Preview> artists = service.getArtistsByGenre(genre, offset, limit);

        model.addAttribute("artists", artists);
        model.addAttribute("title", genre + " artists");

        //Used for pagination
        String prevLink = "/genres/" + genre + "?p=" + (page-1);
        String nextLink = "/genres/" + genre + "?p=" + (page+1);
        model.addAttribute("prevLink", prevLink);
        model.addAttribute("nextLink", nextLink);

        //if(artists.size() + (DatabaseQueries.SMALL_SAMPLE_SIZE * --page) < service.getNumArtistsByGenre(genre))
          //  model.addAttribute("hasNext", true);
        return "artists";
    }

}
