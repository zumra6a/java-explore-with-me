package ru.practicum.ewm.comment.service;

import java.util.List;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentResponseDto;

public interface CommentService {
    CommentResponseDto commentEvent(Long userId, Long eventId, CommentDto commentDto);

    List<CommentResponseDto> getEventComments(Long eventId, Integer from, Integer size);

    CommentResponseDto updateUserComment(Long userId, Long commentId, CommentDto commentDto);

    void deleteUserComment(Long userId, Long commentId);

    List<CommentResponseDto> findAllByUserId(Long userId);

    CommentResponseDto findByUserIdAndCommentId(Long userId, Long commentId);

    void deleteById(Long commentId);

    CommentResponseDto updateById(Long commentId, CommentDto commentDto);
}
