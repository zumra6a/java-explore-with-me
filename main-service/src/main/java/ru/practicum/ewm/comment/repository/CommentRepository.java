package ru.practicum.ewm.comment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.EventCommentCount;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEvent_Id(Long eventId);

    List<Comment> findAllByEvent_Id(Long eventId, Pageable pageable);

    List<Comment> findByAuthor_Id(Long userId);

    Optional<Comment> findByAuthor_IdAndId(Long userId, Long id);

    @Query("SELECT c.event.id as eventId, COUNT(c) as count FROM comments as c " +
            "WHERE c.event.id IN ?1 " +
            "GROUP BY c.event.id")
    List<EventCommentCount> countCommentByEvent(List<Long> eventIds);
}
