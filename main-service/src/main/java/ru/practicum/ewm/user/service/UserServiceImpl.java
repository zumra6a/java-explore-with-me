package ru.practicum.ewm.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public UserDto add(UserDto userDto) {
        User user = userRepository.save(UserMapper.toUser(userDto));

        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public void deleteById(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("No such user with id = " + userId));

        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> findAll(Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);

        return userRepository.findAll(page)
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findAllByIds(List<Long> ids, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);

        return userRepository.findByIdIn(ids, page)
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
