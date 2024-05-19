package ru.practicum.ewm.comment.controller;

import java.util.List;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentResponseDto;
import ru.practicum.ewm.comment.service.CommentService;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/comments")
public class CommentPublicController {
    private final CommentService commentService;

    @Autowired
    public CommentPublicController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{eventId}")
    public List<CommentResponseDto> getRequestListAllCommentsEvent(@PathVariable @Positive Long eventId,
                                                                   @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                                   @RequestParam(defaultValue = "10") @Positive Integer size) {
        return commentService.getEventComments(eventId, from, size);
    }
}
