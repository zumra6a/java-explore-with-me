package ru.practicum.ewm.compilation.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import ru.practicum.ewm.event.dto.EventShortDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationResponseDto {
    private Long id;

    private String title;

    private Boolean pinned;

    private Set<EventShortDto> events;
}

