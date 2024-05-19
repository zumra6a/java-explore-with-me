package ru.practicum.ewm.category.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventsRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventsRepository) {
        this.categoryRepository = categoryRepository;
        this.eventsRepository = eventsRepository;
    }

    @Override
    public List<CategoryDto> findAll(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageRequest).stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NoSuchElementException("No such category with id " + catId));

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto add(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NoSuchElementException("No such category with id " + catId));

        List<Event> events = eventsRepository.findByCategory(category);
        if (!events.isEmpty()) {
            throw new ConflictException("Can't delete category due to using for some events");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto update(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NoSuchElementException("No such category with id " + catId));

        String newName = categoryDto.getName();

        if (newName != null && !category.getName().equals(newName)) {
            if (categoryRepository.existsByNameIgnoreCase(newName)) {
                throw new ConflictException("Category already exists " + newName);
            }
        }

        category.setName(newName);

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }
}
