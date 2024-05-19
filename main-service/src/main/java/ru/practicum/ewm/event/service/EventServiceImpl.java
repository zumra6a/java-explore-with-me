package ru.practicum.ewm.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.Stats;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.EventCommentCount;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.SortType;
import ru.practicum.ewm.event.dto.CaseUpdatedStatusDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationDto;
import ru.practicum.ewm.event.dto.EventRequestsConfirmationResultDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.UpdateEventRequestDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.EventStatus;
import ru.practicum.ewm.request.RequestStatus;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            StatsClient statsClient,
            RequestRepository requestRepository,
            LocationRepository locationRepository,
            CommentRepository commentRepository,
            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.statsClient = statsClient;
        this.requestRepository = requestRepository;
        this.locationRepository = locationRepository;
        this.commentRepository = commentRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EventFullDto> getAllEventFromAdmin(
            List<Long> users,
            List<EventStatus> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size) {
        PageRequest pageable = PageRequest.of(from / size, size);

        Specification<Event> specification = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            specification = specification
                    .and((root, query, criteriaBuilder) -> root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            specification = specification
                    .and((root, query, criteriaBuilder) -> root.get("eventStatus").in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            specification = specification
                    .and((root, query, criteriaBuilder) -> root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            specification = specification
                    .and((root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            specification = specification
                    .and((root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent()
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCountMap = getConfirmedRequestsCount(events.toList());

        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }

        return result;
    }

    @Override
    public EventFullDto updateEventFromAdmin(Long eventId, UpdateEventRequestDto updateEventRequestDto) {
        Event oldEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));

        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED) || oldEvent.getEventStatus().equals(EventStatus.CANCELED)) {
            throw new ConflictException("Event is not published");
        }

        boolean hasChanges = false;

        Event eventForUpdate = universalUpdate(oldEvent, updateEventRequestDto);

        if (eventForUpdate == null) {
            eventForUpdate = oldEvent;
        } else {
            hasChanges = true;
        }

        LocalDateTime eventDate = updateEventRequestDto.getEventDate();

        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("Date parameter is incorrect");
            }
            eventForUpdate.setEventDate(updateEventRequestDto.getEventDate());
            hasChanges = true;
        }

        EventState gotAction = updateEventRequestDto.getStateAction();
        if (gotAction != null) {
            if (EventState.PUBLISH_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventStatus.PUBLISHED);
                hasChanges = true;
            } else if (EventState.REJECT_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventStatus.CANCELED);
                hasChanges = true;
            }
        }
        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }

        return eventAfterUpdate != null
                ? EventMapper.toEventFullDto(eventAfterUpdate)
                : null;
    }

    @Override
    public EventFullDto updateEventByUserIdAndEventId(
            Long userId,
            Long eventId,
            UpdateEventRequestDto updateEventRequestDto) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));
        Event oldEvent = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NoSuchElementException("No such user event"));

        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Published events are not allowed to update");
        }

        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Event is not owned by user = " + userId);
        }

        Event eventForUpdate = universalUpdate(oldEvent, updateEventRequestDto);
        boolean hasChanges = false;
        if (eventForUpdate == null) {
            eventForUpdate = oldEvent;
        } else {
            hasChanges = true;
        }
        LocalDateTime newDate = updateEventRequestDto.getEventDate();
        if (newDate != null) {
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new IllegalArgumentException("Date has not occurred yet");
            }

            eventForUpdate.setEventDate(newDate);
            hasChanges = true;
        }

        EventState stateAction = updateEventRequestDto.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.PENDING);
                    hasChanges = true;
                    break;
                case CANCEL_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.CANCELED);
                    hasChanges = true;
                    break;
            }
        }

        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }

        return eventAfterUpdate != null
                ? EventMapper.toEventFullDto(eventAfterUpdate)
                : null;
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        return eventRepository.findAll(pageRequest)
                .getContent()
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NoSuchElementException("No such user event"));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto add(Long userId, EventDto eventDto) {
        Long catId = eventDto.getCategory();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NoSuchElementException("No such category with id =" + catId));

        if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Date has not occurred yet");
        }

        Event event = EventMapper.toEvent(eventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventStatus(EventStatus.PENDING);
        event.setCreatedDate(LocalDateTime.now());

        if (eventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(eventDto.getLocation()));
            event.setLocation(location);
        }
        Event eventSaved = eventRepository.save(event);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setConfirmedRequests(0);

        return eventFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NoSuchElementException("No such user event"));

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    public EventRequestsConfirmationResultDto updateStatusRequest(Long userId,
                                                                  Long eventId,
                                                                  EventRequestsConfirmationDto eventRequestsConfirmationDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId)
                .orElseThrow(() -> new NoSuchElementException("No such user event"));

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Event does not require confirmation");
        }
        RequestStatus status = eventRequestsConfirmationDto.getStatus();

        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() == confirmedRequestsCount) {
            throw new ConflictException("Participants limit is exceed");
        }

        switch (status) {
            case CONFIRMED:
                CaseUpdatedStatusDto updatedStatusConfirmed = updatedStatusConfirmed(
                        event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(eventRequestsConfirmationDto.getRequestIds()))
                                .build(),
                        RequestStatus.CONFIRMED,
                        confirmedRequestsCount);

                List<Request> confirmedRequests =
                        requestRepository.findAllById(updatedStatusConfirmed.getProcessedIds());
                List<Request> rejectedRequests = new ArrayList<>();

                if (updatedStatusConfirmed.getIdsFromUpdateStatus().size() != 0) {
                    List<Long> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = rejectRequest(ids, eventId);
                }

                return EventRequestsConfirmationResultDto.builder()
                        .confirmedRequests(
                                confirmedRequests
                                        .stream()
                                        .map(RequestMapper::toParticipationRequestDto)
                                        .collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .build();
            case REJECTED:
                CaseUpdatedStatusDto updatedStatusReject = updatedStatusConfirmed(
                        event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(eventRequestsConfirmationDto.getRequestIds()))
                                .build(),
                        RequestStatus.REJECTED,
                        confirmedRequestsCount);
                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestsConfirmationResultDto.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto)
                                .collect(Collectors.toList()))
                        .build();
            default:
                throw new IllegalArgumentException("Incorrect status " + status);
        }
    }

    @Override
    public List<EventShortDto> getAllEventFromPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            SortType sort,
            Integer from,
            Integer size
    ) {
        Sort eventSort = Sort.by(sort.equals(SortType.VIEWS) ? "views" : "eventDate");

        Pageable pageable = PageRequest.of(from / size, size, eventSort);

        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (text != null) {
            String searchText = text.toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(rangeStart, now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("eventStatus"), EventStatus.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        Map<Long, Long> viewStatsMap = getViewsAllEvents(resultEvents);
        Map<Long, Long> eventCommentsCountMap = countCommentsAllEvents(resultEvents);

        List<EventShortDto> result = resultEvents
                .stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        for (EventShortDto event : result) {
            Long viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0L);
            Long eventCommentsCountFromMap = eventCommentsCountMap.getOrDefault(event.getId(), 0L);

            event.setViews(viewsFromMap);
            event.setComments(eventCommentsCountFromMap);
        }

        return result;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));

        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new NoSuchElementException("No such published event with id = " + eventId);
        }

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        Map<Long, Long> viewStatsMap = getViewsAllEvents(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 0L);
        eventFullDto.setViews(views);

        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId);
        eventFullDto.setComments(comments
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList()));

        return eventFullDto;
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getEndpointHit(
                    earliestDate,
                    LocalDateTime.now(),
                    uris,
                    true);

            List<Stats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {});

            viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            Stats::getHits
                    ));
        }

        return viewStatsMap;
    }

    private Map<Long, Long> countCommentsAllEvents(List<Event> resultEvents) {
        List<EventCommentCount> commentsCountMap = commentRepository.countCommentByEvent(
                resultEvents
                        .stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()));

        return commentsCountMap
                .stream()
                .collect(Collectors.toMap(
                        EventCommentCount::getEventId,
                        EventCommentCount::getCount
                ));
    }

    private CaseUpdatedStatusDto updatedStatusConfirmed(Event event, CaseUpdatedStatusDto caseUpdatedStatus,
                                                        RequestStatus status, int confirmedRequestsCount) {
        List<Long> ids = caseUpdatedStatus.getIdsFromUpdateStatus();
        List<Request> requestListLoaded = requestRepository.findByEventIdAndIdIn(event.getId(), ids)
                .orElseThrow(() -> new NoSuchElementException("No such requests"));

        List<Long> processedIds = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();

        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;

        for (Request request : requestListLoaded) {
            if (freeRequest == 0) {
                break;
            }

            request.setStatus(status);
            requestList.add(request);

            processedIds.add(request.getId());
            freeRequest--;
        }

        requestRepository.saveAll(requestList);
        caseUpdatedStatus.setProcessedIds(processedIds);

        return caseUpdatedStatus;
    }

    private List<Request> rejectRequest(List<Long> ids, Long eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = requestRepository.findByEventIdAndIdIn(eventId, ids)
                .orElseThrow(() -> new NoSuchElementException("No such events"));

        for (Request request : requestListLoaded) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }
            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestRepository.saveAll(requestList);

        return rejectedRequests;
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream()
                .map(Event::getId)
                .collect(Collectors.toList()), RequestStatus.CONFIRMED);

        return requests
                .stream()
                .collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private Event universalUpdate(Event oldEvent, UpdateEventRequestDto updateEvent) {
        boolean hasChanges = false;
        String gotAnnotation = updateEvent.getAnnotation();
        if (gotAnnotation != null && !gotAnnotation.isBlank()) {
            oldEvent.setAnnotation(gotAnnotation);
            hasChanges = true;
        }
        Long catId = updateEvent.getCategory();
        if (catId != null) {
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NoSuchElementException("No such category with id =" + catId));

            oldEvent.setCategory(category);
            hasChanges = true;
        }
        String gotDescription = updateEvent.getDescription();
        if (gotDescription != null && !gotDescription.isBlank()) {
            oldEvent.setDescription(gotDescription);
            hasChanges = true;
        }
        if (updateEvent.getLocation() != null) {
            Location location = LocationMapper.toLocation(updateEvent.getLocation());
            oldEvent.setLocation(location);
            hasChanges = true;
        }
        Integer gotParticipantLimit = updateEvent.getParticipantLimit();
        if (gotParticipantLimit != null) {
            oldEvent.setParticipantLimit(gotParticipantLimit);
            hasChanges = true;
        }
        if (updateEvent.getPaid() != null) {
            oldEvent.setPaid(updateEvent.getPaid());
            hasChanges = true;
        }
        Boolean requestModeration = updateEvent.getRequestModeration();
        if (requestModeration != null) {
            oldEvent.setRequestModeration(requestModeration);
            hasChanges = true;
        }
        String gotTitle = updateEvent.getTitle();
        if (gotTitle != null && !gotTitle.isBlank()) {
            oldEvent.setTitle(gotTitle);
            hasChanges = true;
        }
        if (!hasChanges) {
            oldEvent = null;
        }

        return oldEvent;
    }
}
