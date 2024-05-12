package ru.practicum.ewm.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.dto.Stats;

@Repository
public class StatsRepositoryImpl implements StatsRepository {
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public StatsRepositoryImpl(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public void save(EndpointHit endpointHit) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "app", endpointHit.getApp(),
                "uri", endpointHit.getUri(),
                "ip", endpointHit.getIp(),
                "created", Timestamp.valueOf(endpointHit.getTimestamp())
        ));

        String query = "INSERT INTO endpoint_hits (app, uri, ip, created) VALUES (:app, :uri, :ip, :created)";

        namedJdbcTemplate.update(query, parameters);
    }

    @Override
    public List<Stats> getStats(LocalDateTime start, LocalDateTime end) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "start", start,
                "end", end
        ));

        String query = "SELECT app, uri, COUNT(uri) as hits FROM endpoint_hits " +
                "WHERE created BETWEEN :start AND :end " +
                "GROUP BY app, uri " +
                "ORDER BY hits DESC";

        return namedJdbcTemplate.query(query, parameters, this::mapRow);
    }

    @Override
    public List<Stats> getStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "start", start,
                "end", end,
                "uris", uris
        ));

        String query = "SELECT app, uri, COUNT(uri) as hits FROM endpoint_hits " +
                "WHERE created BETWEEN :start AND :end AND uri IN (:uris) " +
                "GROUP BY app, uri " +
                "ORDER BY hits DESC";

        return namedJdbcTemplate.query(query, parameters, this::mapRow);
    }

    @Override
    public List<Stats> getUniqueStats(LocalDateTime start, LocalDateTime end) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "start", start,
                "end", end
        ));

        String query = "SELECT app, uri, COUNT (DISTINCT ip) AS hits FROM endpoint_hits " +
                "WHERE created BETWEEN :start AND :end " +
                "GROUP BY app, uri " +
                "ORDER BY hits DESC";

        return namedJdbcTemplate.query(query, parameters, this::mapRow);
    }

    @Override
    public List<Stats> getUniqueStatsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris) {
        SqlParameterSource parameters = new MapSqlParameterSource(Map.of(
                "start", start,
                "end", end,
                "uris", uris
        ));

        String query = "SELECT app, uri, COUNT (DISTINCT ip) AS hits FROM endpoint_hits " +
                "WHERE created BETWEEN :start AND :end AND uri IN (:uris) " +
                "GROUP BY app, uri " +
                "ORDER BY hits DESC";

        return namedJdbcTemplate.query(query, parameters, this::mapRow);
    }

    private Stats mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Stats.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getInt("hits"))
                .build();
    }
}
