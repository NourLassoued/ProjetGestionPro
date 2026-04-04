package com.example.gestionprojeet.demo;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@AllArgsConstructor
@RequestMapping("/api/v1/DemoControlleur")
public class DemoControlleur {
    @GetMapping
    public ResponseEntity<String> sayHEllo(){
        return ResponseEntity.ok("hello from secured endPoint");
    }
}
