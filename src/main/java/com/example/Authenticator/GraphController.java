package com.example.Authenticator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
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
