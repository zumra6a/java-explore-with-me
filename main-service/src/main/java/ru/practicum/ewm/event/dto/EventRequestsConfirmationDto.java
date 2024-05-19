package ru.practicum.ewm.event.dto;

import lombok.*;
import ru.practicum.ewm.request.RequestStatus;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestsConfirmationDto {
    private Set<Long> requestIds;

    private RequestStatus status;
}
