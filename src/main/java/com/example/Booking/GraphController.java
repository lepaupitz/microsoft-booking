package com.example.Booking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class GraphController {

    @Autowired
    private GraphService graphService;

    @GetMapping("/me")
    public String getMe() {
        try {
            return graphService.GraphApiClient();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
