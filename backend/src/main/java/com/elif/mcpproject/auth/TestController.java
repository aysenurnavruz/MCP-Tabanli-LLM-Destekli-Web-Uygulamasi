package com.elif.mcpproject.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public String secure(){
        return "JWT works 🎉";
    }

    @GetMapping("/me")
    public String me(Principal principal) {
        return principal.getName();
    }
}
