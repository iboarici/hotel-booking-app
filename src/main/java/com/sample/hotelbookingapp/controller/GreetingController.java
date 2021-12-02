package com.sample.hotelbookingapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";

    @GetMapping("/greeting")
    public String greet(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format(template, name);
    }
}
