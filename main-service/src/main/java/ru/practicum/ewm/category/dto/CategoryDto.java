package ru.practicum.ewm.category.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
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
public class CategoryDto {
    @Positive(
            message = "Id should not be empty",
            groups = {Marker.OnUpdate.class})
    private Long id;

    @NotBlank(
            message = "Name should not be empty",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    @Size(
            message = "Name should have more than 50 chars",
            min = 1,
            max = 50,
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String name;
}
