package com.example.leastscore.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ShareLinkController {
  @GetMapping({"/r/{roomCode}", "/room/{roomCode}"})
  public String room(@PathVariable String roomCode) {
    return "forward:/index.html";
  }
}

