package ru.practicum.ewm.compilation.dto;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.validation.Marker;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    private Set<Long> events;

    private Boolean pinned;

    @Pattern(
            message = "Title should not consist only of whitespace characters",
            regexp = "^(?!\\s*$).+",
            groups = Marker.OnUpdate.class)
    @NotBlank(
            message = "Title should not be empty",
            groups = {Marker.OnCreate.class})
    @Size(
            message = "Title should have more than 50 chars",
            min = 1,
            max = 50,
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String title;
}
