package com.spotifydb.ui.controllers;

import com.spotifydb.model.Preview;
import com.spotifydb.model.db.implementations.DatabaseConnection;
import com.spotifydb.model.db.queries.DatabaseQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.spotifydb.application.DatabaseService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.spotifydb.model.db.implementations.DatabaseConnection.RESULTS_PER_PAGE;


@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private DatabaseService service;

    @GetMapping("")
    public String searchGet(Model model, @RequestParam(required = false) String type,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) String genre,
                            @RequestParam(required = false, defaultValue = "1") Integer page){

        if (type != null){
            List<Preview> previews = null;
            boolean hasNext = false;
            switch (type){
                case "artist":
                    int offset = RESULTS_PER_PAGE * (page - 1);
                    previews = service.getArtists(genre, name, offset, RESULTS_PER_PAGE);
                    hasNext = service.getNumArtistsBy(genre, name) > previews.size() + RESULTS_PER_PAGE * (page -1);
                    break;
                default:
                    break;
            }

            if (previews != null){
                model.addAttribute("artists", previews);
                model.addAttribute("title", "Search");


                boolean hasPrev = page >= 2 && previews.size() > 0;
                if (hasPrev){
                    String prevLink = getPaginationLink( page - 1);
                    model.addAttribute("prevLink", prevLink);
                    model.addAttribute("hasPrev", true);
                }

                if (hasNext){
                    String nextLink = getPaginationLink(page + 1);
                    model.addAttribute("nextLink", nextLink);
                    model.addAttribute("hasNext", true);
                }


                return "artists";
            }
        }

        model.addAttribute("title", "Search");
        return "search";
    }

    private static String getPaginationLink(int page){
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        //builder.scheme("https");
        builder.replaceQueryParam("page", page);
        return builder.build().toString();
    }

}
