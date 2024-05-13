package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto add(UserDto userDto);

    void deleteById(Long userId);

    List<UserDto> findAll(Integer from, Integer size);

    List<UserDto> findAllByIds(List<Long> ids, Integer from, Integer size);
}
