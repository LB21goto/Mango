package com.example.controller;

import com.example.dto.ProgramDTO;
import com.example.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/program")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;

    @GetMapping("/{id}")
    public ProgramDTO getProgramDetail(@PathVariable Long id) {
        return programService.getProgramById(id);
    }
}
