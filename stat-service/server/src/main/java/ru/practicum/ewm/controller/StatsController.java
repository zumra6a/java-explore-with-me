package ru.practicum.ewm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.Constants;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.Stats;
import ru.practicum.ewm.service.StatsService;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class StatsController {
    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createEndpointHit(@RequestBody EndpointHit endpointHit) {
        log.info("Request to create endpoint hit {}", endpointHit);

        statsService.save(endpointHit);
    }

    @GetMapping("/stats")
    public List<Stats> getStats(@RequestParam @DateTimeFormat(pattern = Constants.DATE_FORMAT) LocalDateTime start,
                                @RequestParam @DateTimeFormat(pattern = Constants.DATE_FORMAT) LocalDateTime end,
                                @RequestParam(required = false) List<String> uris,
                                @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Request to get stats");

        if (end.isBefore(start)) {
            log.info("Uncorrected format of dates start {} и end {}", start, end);

            throw new InvalidParameterException("Uncorrected format of dates");
        }

        if (unique) {
            return statsService.getUniqViewStatsList(start, end, uris);
        }

        return statsService.getViewStatsList(start, end, uris);
    }
}
