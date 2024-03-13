package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {
    @GetMapping("/")
    public String redirectRootToIndex() {
        return "redirect:/index.html";
    }
}
