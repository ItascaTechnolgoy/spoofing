package com.itasca.spoofing.service;

import com.itasca.spoofing.model.UserDto;
import com.itasca.spoofing.model.GroupProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface UserService {
    Page<UserDto> getUsers(Pageable pageable);
    Optional<UserDto> getUserById(Long id);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long id, UserDto userDto);
    void deleteUser(Long id);
    UserDto assignProfilesToUser(Long userId, Set<String> profileIds);
    List<GroupProfileDto> getAssignedProfiles(Long userId);
    List<GroupProfileDto> getUnassignedProfiles(Long userId);
}