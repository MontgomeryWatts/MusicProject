package com.spotifydb.ui.controllers;

import com.spotifydb.model.Preview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.spotifydb.application.DatabaseService;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String searchGet(Model model, @RequestParam(required = false) String type, @RequestParam(required = false) String name,
                            @RequestParam(required = false) String genre){

        if (type != null){
            List<Preview> previews = null;
            switch (type){
                case "artist":
                    previews = service.getArtists(genre, name, 0, 20);
                    break;
                default:
                    break;
            }

            if (previews != null){
                model.addAttribute("artists", previews);
                model.addAttribute("title", "Search");
                return "artists";
            }
        }

        model.addAttribute("title", "Search");
        return "search";
    }

}
