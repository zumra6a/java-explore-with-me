package ru.practicum.ewm.dto;


import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(toBuilder = true)
public class StatsRequest {
    private List<String> uris;

    @Builder.Default
    private LocalDateTime start = LocalDateTime.now()
            .withHour(0)
            .withMinute(0)
            .withSecond(0);

    @Builder.Default
    private LocalDateTime end = LocalDateTime.now();

    private boolean unique;

    private String application;
}
