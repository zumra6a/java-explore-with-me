package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationResponseDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationResponseDto add(CompilationDto compilationDto);

    CompilationResponseDto update(Long compId, CompilationDto update);

    void deleteById(Long compId);

    List<CompilationResponseDto> findAll(Boolean pinned, Integer from, Integer size);

    CompilationResponseDto findById(Long compId);
}
