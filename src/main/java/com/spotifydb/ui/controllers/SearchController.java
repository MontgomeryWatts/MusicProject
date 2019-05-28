package com.spotifydb.ui.controllers;

import com.spotifydb.model.PreviewPage;
import com.spotifydb.model.db.implementations.DatabaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.spotifydb.model.db.implementations.DatabaseConnection.RESULTS_PER_PAGE;


@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private DatabaseConnection db;

    @GetMapping("")
    public String searchGet(Model model, @RequestParam(required = false) String type,
                            @RequestParam(required = false) String name,
                            @RequestParam(required = false) String genre,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false, defaultValue = "1") Integer page){

        model.addAttribute("title", "Search - SpotifyDB");

        if (type != null){
            PreviewPage previewPage = null;
            int offset = RESULTS_PER_PAGE * (page - 1);
            switch (type){
                case "artist":
                    previewPage = db.getArtists(genre, name, offset, RESULTS_PER_PAGE);
                    break;
                case "album":
                    previewPage = db.getAlbums(name, year, offset, RESULTS_PER_PAGE);
                default:
                    break;
            }

            if (previewPage != null){
                model.addAttribute("results", previewPage.getPreviews());
                model.addAttribute("page", page);


                boolean hasPrev = page >= 2 && previewPage.hasItems();
                if (hasPrev){
                    String prevLink = getPaginationLink( page - 1);
                    model.addAttribute("prevLink", prevLink);
                    model.addAttribute("hasPrev", true);
                }

                if (previewPage.hasNext()){
                    String nextLink = getPaginationLink(page + 1);
                    model.addAttribute("nextLink", nextLink);
                    model.addAttribute("hasNext", true);
                }


                return "results";
            }
        }

        return "search";
    }

    private static String getPaginationLink(int page){
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        builder.scheme("https");
        builder.replaceQueryParam("page", page);
        return builder.build().toString();
    }

}
