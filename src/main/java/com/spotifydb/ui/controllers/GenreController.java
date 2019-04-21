package com.spotifydb.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
