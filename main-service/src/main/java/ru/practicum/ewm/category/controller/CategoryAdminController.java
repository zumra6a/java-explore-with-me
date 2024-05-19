package ru.practicum.ewm.category.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.validation.Marker;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import javax.validation.constraints.Min;

@Slf4j
@Validated
@RestController
@RequestMapping(path = "/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto add(@Validated(Marker.OnCreate.class) @RequestBody CategoryDto categoryDto) {
        log.info("Got request to add category: {}", categoryDto);

        return categoryService.add(categoryDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@Validated(Marker.OnUpdate.class) @RequestBody CategoryDto categoryDto,
                              @PathVariable(value = "catId") @Min(1) Long catId) {
        log.info("Got request to update category with id: {} to {}", catId, categoryDto);

        return categoryService.update(catId, categoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable @Min(1) Long catId) {
        log.info("Got request to delete category with id: {}", catId);

        categoryService.deleteById(catId);
    }
}
