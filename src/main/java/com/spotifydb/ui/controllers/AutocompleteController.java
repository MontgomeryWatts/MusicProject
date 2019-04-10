package com.spotifydb.ui.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.spotifydb.application.DatabaseService;


import java.util.List;

@Controller
@RequestMapping("/autocomplete")
public class AutocompleteController {

    @Autowired
    private DatabaseService service;

    @PostMapping("")
    @ResponseBody
    public Iterable<String> getAjaxResult(@RequestBody String query){
        int equalsIndex = query.indexOf('='); // The query is passed in as q={query}
        query = query.substring(equalsIndex + 1);
        return service.getSimilarArtistNames(query, 0, 10);
    }

}
