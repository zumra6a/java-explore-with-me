package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> findAll(Integer from, Integer size);

    CategoryDto findById(Long catId);

    CategoryDto add(CategoryDto categoryDto);

    void deleteById(Long catId);

    CategoryDto update(Long catId, CategoryDto categoryDto);
}
