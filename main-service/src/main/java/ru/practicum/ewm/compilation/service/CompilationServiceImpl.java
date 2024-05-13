package ru.practicum.ewm.compilation.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationResponseDto;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.repository.EventRepository;

@Service
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public CompilationResponseDto add(CompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);

        boolean pinned = compilationDto.getPinned() != null
                ? compilationDto.getPinned()
                : false;
        compilation.setPinned(pinned);

        Set<Long> compEventIds = compilationDto.getEvents() != null
                ? compilationDto.getEvents()
                : Collections.emptySet();
        List<Long> eventIds = new ArrayList<>(compEventIds);
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        Set<Event> eventsSet = new HashSet<>(events);
        compilation.setEvents(eventsSet);

        return CompilationMapper.toCompilationResponseDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public CompilationResponseDto update(Long compId, CompilationDto compilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("No such compilation with id " + compId));

        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }

        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }

        Set<Long> eventIds = compilationDto.getEvents();

        if (eventIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            Set<Event> eventSet = new HashSet<>(events);
            compilation.setEvents(eventSet);
        }

        return CompilationMapper.toCompilationResponseDto(compilation);
    }

    @Transactional
    @Override
    public void deleteById(Long compId) {
        compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("No such compilation with id " + compId));

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationResponseDto> findAll(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from, size);

        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAll(pageRequest).getContent()
                : compilationRepository.findAllByPinned(pinned, pageRequest);

        return compilations.stream()
                .map(CompilationMapper::toCompilationResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationResponseDto findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NoSuchElementException("No such compilation with id " + compId));

        return CompilationMapper.toCompilationResponseDto(compilation);
    }
}
