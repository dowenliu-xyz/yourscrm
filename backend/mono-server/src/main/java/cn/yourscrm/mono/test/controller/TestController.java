package cn.yourscrm.mono.test.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @PreAuthorize("hasRole('ROLE_OPS')")
    @GetMapping("/ops/hello")
    public String opsHello() {
        return "Hello, ops!";
    }
}
