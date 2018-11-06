package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import spring.services.MongoService;

import java.util.List;

@Controller
public class GenreController {

    @Autowired
    private MongoService service;

    @GetMapping("/genres/{genre}")
    public String artistsGenre(Model model, @PathVariable String genre){
        List<Document> artists = service.getArtistsByGenre(genre);
        model.addAttribute("artists", artists);
        return "artists";
    }
}
