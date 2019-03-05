package spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.DatabaseService;


@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String searchGet(Model model, @RequestParam(name = "artist_name", required = false) String artistName){
        if (artistName != null){
            int offset = 0;
            int limit = Integer.MAX_VALUE;
            model.addAttribute("artists", service.getArtistsByName(artistName, offset, limit));
            model.addAttribute("title", "Search Artists");
            return "artists";
        }
        model.addAttribute("title", "Artist search");
        return "search";
    }

}
