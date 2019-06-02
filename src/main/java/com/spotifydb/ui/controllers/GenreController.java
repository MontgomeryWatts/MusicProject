package com.spotifydb.ui.controllers;

import com.spotifydb.model.db.implementations.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private DatabaseConnection db;

    @GetMapping("")
    public String genres(Model model, @RequestParam(name = "letter", required = false) String letter){
        List<String> genres;

        List<Character> alphabet = new ArrayList<>();
        for(char c = 'A'; c <= 'Z'; c++)
            alphabet.add(c);
        model.addAttribute("alphabet", alphabet);

        if (letter != null){
            char c = letter.charAt(0);
            genres = db.getGenresByLetter(c);
            model.addAttribute("title", "Genres starting with " + c);
        } else{
            genres = db.getGenres();
            model.addAttribute("title", "All genres");
        }

        model.addAttribute("genres", genres);
        return "genres";
    }
}
