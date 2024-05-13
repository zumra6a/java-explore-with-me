package ru.practicum.ewm.service;

import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void save(EndpointHit hit);

    List<Stats> getViewStatsList(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<Stats> getUniqViewStatsList(LocalDateTime start, LocalDateTime end, List<String> uris);
}
