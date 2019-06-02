package com.spotifydb.ui.controllers;


import com.spotifydb.model.db.implementations.DatabaseConnection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/albums")
public class AlbumController {

    @Autowired
    private DatabaseConnection db;


    @GetMapping("/{id}")
    public String artistsUri(Model model, @PathVariable String id){
        Document artist = db.getAlbumPage(id);
        model.addAttribute("artist", artist);
        return "album";
    }
}
