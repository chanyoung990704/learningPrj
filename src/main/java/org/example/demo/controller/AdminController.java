package org.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {


    @GetMapping("/users")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String users(@AuthenticationPrincipal UserDetails userDetails) {
        log.info(userDetails.getUsername());
        return userDetails.getAuthorities().toString();
    }
}
