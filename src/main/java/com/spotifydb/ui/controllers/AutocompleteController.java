package com.spotifydb.ui.controllers;

import com.spotifydb.model.db.implementations.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/autocomplete")
public class AutocompleteController {

    @Autowired
    private DatabaseConnection db;

    @PostMapping("")
    @ResponseBody
    public Iterable<String> getAjaxResult(@RequestBody String query){
        int equalsIndex = query.indexOf('='); // The query is passed in as q={query}
        query = query.substring(equalsIndex + 1);
        return db.getSimilarArtistNames(query, 0, 10);
    }

}
