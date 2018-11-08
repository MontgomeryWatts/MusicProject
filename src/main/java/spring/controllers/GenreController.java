package spring.controllers;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.MongoService;

import java.util.List;

@Controller
public class GenreController {

    @Autowired
    private MongoService service;

    @GetMapping("/genres")
    public String genres(Model model){
        List<String> genres = service.getGenres();
        model.addAttribute("genres", genres);
        return "genres";
    }

    @GetMapping("/genres/{genre}")
    public String genre(Model model, @PathVariable String genre, @RequestParam(name = "p", required = false, defaultValue = "1") String page_number){
        int number = 1;
        if (page_number.matches("\\d+"))
            number = Integer.parseInt(page_number);

        List<Document> artists = service.getArtistsByGenre(genre, number);
        model.addAttribute("artists", artists);
        return "artists";
    }


}
