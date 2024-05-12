package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Stats {
    private String app;

    private String uri;

    private int hits;
}
