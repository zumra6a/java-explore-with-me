package ru.practicum.ewm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.dto.Stats;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statRepository;

    @Autowired
    public StatsServiceImpl(StatsRepository statRepository) {
        this.statRepository = statRepository;
    }

    @Override
    public void save(EndpointHit endpointHit) {
        statRepository.save(endpointHit);
    }

    @Override
    public List<Stats> getViewStatsList(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return uris == null
                ? statRepository.getStats(start, end)
                : statRepository.getStatsWithUris(start, end, uris);
    }

    @Override
    public List<Stats> getUniqViewStatsList(LocalDateTime start, LocalDateTime end, List<String> uris) {
        return uris == null
                ? statRepository.getUniqueStats(start, end)
                : statRepository.getUniqueStatsWithUris(start, end, uris);
    }

}
