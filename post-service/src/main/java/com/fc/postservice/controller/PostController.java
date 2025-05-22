package com.fc.postservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {


    @GetMapping("/post")
    public String getProductsReviewHandler(){

        return "Hello everyone";
    }

}
