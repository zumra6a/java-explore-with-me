package ru.practicum.ewm.comment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentResponseDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.EventStatus;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

@Service
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(
            UserRepository userRepository,
            EventRepository eventRepository,
            CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public CommentResponseDto commentEvent(Long userId, Long eventId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));

        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new IllegalArgumentException("Event is not published");
        }

        Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, event, user));

        return CommentMapper.toCommentResponseDto(comment);
    }

    @Override
    public List<CommentResponseDto> getEventComments(Long eventId, Integer from, Integer size) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No such event with id = " + eventId));

        PageRequest pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllByEvent_Id(eventId, pageable)
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto updateUserComment(Long userId, Long commentId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("No such comment with id = " + commentId));

        if (!comment.getAuthor().equals(user)) {
            throw new IllegalArgumentException("Comment is not owned by user");
        }

        comment.setText(commentDto.getText());
        comment.setLastUpdatedOn(LocalDateTime.now());

        return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    public void deleteUserComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("No such comment with id = " + commentId));

        if (!comment.getAuthor().equals(user)) {
            throw new IllegalArgumentException("Comment is not owned by user");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentResponseDto> findAllByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));

        return commentRepository.findByAuthor_Id(userId)
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto findByUserIdAndCommentId(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));

        Comment comment = commentRepository.findByAuthor_IdAndId(userId, commentId)
                .orElseThrow(() -> new NoSuchElementException("No such comment with id = " + commentId));

        return CommentMapper.toCommentResponseDto(comment);
    }

    @Override
    public CommentResponseDto updateById(Long commentId, CommentDto commentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("No such comment with id = " + commentId));


        comment.setText(commentDto.getText());
        comment.setLastUpdatedOn(LocalDateTime.now());

        return CommentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    @Override
    public void deleteById(Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("No such comment with id = " + commentId));

        commentRepository.deleteById(commentId);
    }
}
