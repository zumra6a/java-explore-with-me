package ru.practicum.ewm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Component
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

    @Value("${server.application.name:ewm-main-service}")
    private String applicationName;

    @Autowired
    public StatsClient(@Value("${stat-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                    .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                    .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                    .build());
    }

    public ResponseEntity<Object> saveEndpointHit(EndpointHit endpointHit) {
        return post("/hit", endpointHit);
    }

    public ResponseEntity<Object> getEndpointHit(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
        );

        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}
