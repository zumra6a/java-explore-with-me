package ru.practicum.ewm.compilation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.compilation.dto.CompilationResponseDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import ru.practicum.ewm.validation.Marker;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationService compilationService;

    @Autowired
    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponseDto add(@Validated(Marker.OnCreate.class) @RequestBody CompilationDto compilationDto) {
        log.info("Got request to add compilation");

        return compilationService.add(compilationDto);
    }

    @PatchMapping("/{compId}")
    public CompilationResponseDto update(@Validated(Marker.OnUpdate.class) @RequestBody CompilationDto compilationDto,
                                         @PathVariable Long compId) {
        log.info("Got request to compilationDto compilation with id: {} to {}", compId, compilationDto);

        return compilationService.update(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long compId) {
        log.info("Got request to delete compilation with id: {}", compId);

        compilationService.deleteById(compId);
    }
}
