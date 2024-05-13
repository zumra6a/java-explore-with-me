package ru.practicum.ewm.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationResultDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequestDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    @Autowired
    public EventPrivateController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventShortDto> getEventsByUserId(@PathVariable(value = "userId") @Min(1) Long userId,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero Integer from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                 @Positive Integer size) {
        log.info("Request to get user {} events", userId);

        return eventService.getEventsByUserId(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto add(@PathVariable(value = "userId") @Min(1) Long userId,
                            @RequestBody @Valid EventDto input) {
        log.info("Request to add new event by user {}", userId);

        return eventService.add(userId, input);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUserIdAndEventId(@PathVariable(value = "userId") @Min(1) Long userId,
                                                   @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("Request to get events, user = {}, event = {}", userId, eventId);

        return eventService.getEventByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUserIdAndEventId(@PathVariable(value = "userId") @Min(0) Long userId,
                                                      @PathVariable(value = "eventId") @Min(0) Long eventId,
                                                      @RequestBody @Valid UpdateEventRequestDto updateEventRequestDto) {
        log.info("Request to update events, user = {}, event = {}", userId, eventId);

        return eventService.updateEventByUserIdAndEventId(userId, eventId, updateEventRequestDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByEventFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                       @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("Request to get participation requests");

        return eventService.getAllParticipationRequestsFromEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestsConfirmationResultDto updateStatusRequestFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                           @PathVariable(value = "eventId") @Min(1) Long eventId,
                                                                           @RequestBody EventRequestsConfirmationDto eventRequestsConfirmationDto) {
        log.info("Request to update event of owner {}, event = {}", userId, eventId);

        return eventService.updateStatusRequest(userId, eventId, eventRequestsConfirmationDto);
    }
}
