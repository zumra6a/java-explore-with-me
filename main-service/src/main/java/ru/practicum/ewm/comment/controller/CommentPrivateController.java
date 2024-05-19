package ru.practicum.ewm.comment.controller;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentResponseDto;
import ru.practicum.ewm.comment.service.CommentService;

@Validated
@RestController
@RequestMapping(path = "/comments")
public class CommentPrivateController {
    private final CommentService commentService;

    @Autowired
    public CommentPrivateController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto commentEvent(@PathVariable @Positive Long userId,
                                           @PathVariable @Positive Long eventId,
                                           @RequestBody @Valid CommentDto commentDto) {
        return commentService.commentEvent(userId, eventId, commentDto);
    }

    @PatchMapping("/users/{userId}/{commentId}")
    public CommentResponseDto updateUserComment(@PathVariable @Positive Long userId,
                                                @PathVariable @Positive Long commentId,
                                                @RequestBody @Valid CommentDto commentDto) {
        return commentService.updateUserComment(userId, commentId, commentDto);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentResponseDto> findAllByUserId(@PathVariable @Positive Long userId) {
        return commentService.findAllByUserId(userId);
    }

    @GetMapping("/users/{userId}/{commentId}")
    public CommentResponseDto findByUserIdAndCommentId(@PathVariable @Positive Long userId,
                                                       @PathVariable @Positive Long commentId) {
        return commentService.findByUserIdAndCommentId(userId, commentId);
    }

    @DeleteMapping("/users/{userId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserComment(@PathVariable @Positive Long userId, @PathVariable @Positive Long commentId) {
        commentService.deleteUserComment(userId, commentId);
    }
}
