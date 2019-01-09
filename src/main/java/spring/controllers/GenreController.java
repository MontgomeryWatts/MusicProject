package spring.controllers;

import db.queries.DatabaseQueries;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.MongoService;

import java.util.List;

@Controller
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private MongoService service;

    @GetMapping("")
    public String genres(Model model, @RequestParam(name = "letter", required = false) String letter){
        List<String> genres;
        if (letter != null){
            char c = letter.charAt(0);
            genres = service.getGenresByLetter(c);
        }
        else
          genres = service.getAllGenres();
        model.addAttribute("genres", genres);
        return "genres";
    }

    @GetMapping("/{genre}")
    public String genre(Model model, @PathVariable String genre, @RequestParam(name = "p", required = false, defaultValue = "1") String page_number){
        int number = 1;
        if (page_number.matches("\\d+"))
            number = Integer.parseInt(page_number);

        List<Document> artists = service.getArtistsByGenre(genre, number);
        model.addAttribute("artists", artists);

        //Used for pagination
        String prevLink = "/genres/" + genre + "?p=" + (number-1);
        String nextLink = "/genres/" + genre + "?p=" + (number+1);
        model.addAttribute("prevLink", prevLink);
        model.addAttribute("nextLink", nextLink);

        if(artists.size() + (DatabaseQueries.SMALL_SAMPLE_SIZE * --number) < service.getNumArtistsByGenre(genre))
            model.addAttribute("hasNext", true);
        return "artists";
    }

}
