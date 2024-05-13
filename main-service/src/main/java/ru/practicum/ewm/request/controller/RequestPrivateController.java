package ru.practicum.ewm.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "users/{userId}/requests")
public class RequestPrivateController {
    private final RequestService requestService;

    @Autowired
    public RequestPrivateController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto add(@PathVariable(value = "userId") @Min(0) Long userId,
                                       @RequestParam(name = "eventId") @Min(0) Long eventId) {
        log.info("Request to add user {} request in event {}", userId, eventId);

        return requestService.add(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> findById(@PathVariable(value = "userId") @Min(0) Long userId) {
        log.info("Request to find user request by user {}", userId);

        return requestService.findById(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable(value = "userId") @Min(0) Long userId,
                                          @PathVariable(value = "requestId") @Min(0) Long requestId) {
        log.info("Request to cancel user {} request", userId);

        return requestService.cancel(userId, requestId);
    }
}
