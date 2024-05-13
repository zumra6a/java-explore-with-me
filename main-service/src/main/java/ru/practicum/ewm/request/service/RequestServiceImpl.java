package ru.practicum.ewm.request.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.event.EventStatus;
import ru.practicum.ewm.request.RequestStatus;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public RequestServiceImpl(
            RequestRepository requestRepository,
            UserRepository userRepository,
            EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public ParticipationRequestDto add(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Event is initiated by owner");
        }
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Event is not published");
        }

        int eventCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= eventCount) {
            throw new ConflictException("The participant limit is reached.");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Already added a request for this event.");
        }

        RequestStatus status = event.isRequestModeration()
                ? RequestStatus.PENDING
                : RequestStatus.CONFIRMED;

        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(status)
                .build();

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> findById(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));

        return requestRepository.findAllByRequesterId(userId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NoSuchElementException("No such request with id = " + requestId));

        if (request.getStatus().equals(RequestStatus.CANCELED) || request.getStatus().equals(RequestStatus.REJECTED)) {
            throw new IllegalArgumentException("Request is not confirmed");
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }
}
