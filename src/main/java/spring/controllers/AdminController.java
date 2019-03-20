package spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import spring.services.DatabaseService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DatabaseService database;


    @GetMapping
    public String getPage(){
        // If logged on, if not logged on
        return "login";
    }
}
