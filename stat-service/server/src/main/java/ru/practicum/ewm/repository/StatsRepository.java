package ru.practicum.ewm.repository;

import java.time.LocalDateTime;
import java.util.List;

import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.dto.Stats;

public interface StatsRepository {
    void save(EndpointHit hit);

    List<Stats> getStats(LocalDateTime start, LocalDateTime end);

    List<Stats> getStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    List<Stats> getUniqueStats(LocalDateTime start, LocalDateTime end);

    List<Stats> getUniqueStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
