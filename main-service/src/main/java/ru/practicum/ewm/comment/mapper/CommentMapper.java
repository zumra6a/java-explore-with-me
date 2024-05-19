package ru.practicum.ewm.comment.mapper;

import java.time.LocalDateTime;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentResponseDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

@UtilityClass
public class CommentMapper {
    public CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .createDate(comment.getCreateDate())
                .lastUpdatedOn(comment.getLastUpdatedOn())
                .build();
    }

    public Comment toComment(CommentDto commentDto, Event event, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .author(user)
                .createDate(LocalDateTime.now())
                .lastUpdatedOn(null)
                .build();
    }
}
