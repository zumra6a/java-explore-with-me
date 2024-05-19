package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto add(Long userId, Long eventId);

    List<ParticipationRequestDto> findById(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);
}
