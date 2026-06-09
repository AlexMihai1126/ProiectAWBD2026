package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.model.dto.response.UserOptionResponse;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserOptionResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> UserOptionResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .build())
                .toList();
    }
}
