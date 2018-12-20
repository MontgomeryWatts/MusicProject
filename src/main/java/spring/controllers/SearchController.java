package spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.services.MongoService;


@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private MongoService service;

    @GetMapping("")
    public String searchGet(Model model, @RequestParam(name = "artist_name", required = false) String artistName){
        if (artistName != null){
            model.addAttribute("artists", service.getArtistsByName(artistName));
            return "artists";
        }
        return "search";
    }

}
