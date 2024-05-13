package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.EventStatus;
import ru.practicum.ewm.event.SortType;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationResultDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequestDto;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getAllEventFromAdmin(List<Long> users, List<EventStatus> states, List<Long> categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from,
                                            Integer size);

    EventFullDto updateEventFromAdmin(Long eventId, UpdateEventRequestDto updateEventAdminRequestDto);

    EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventRequestDto updateEventRequestDto);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto add(Long userId, EventDto input);

    EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId);

    EventRequestsConfirmationResultDto updateStatusRequest(Long userId, Long eventId,
                                                           EventRequestsConfirmationDto eventRequestsConfirmationDto);

    List<EventShortDto> getAllEventFromPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, SortType sort, Integer from, Integer size);

    EventFullDto getEventById(Long eventId, HttpServletRequest request);
}
