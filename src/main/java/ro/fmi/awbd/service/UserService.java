package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.fmi.awbd.model.dto.response.UserOptionResponse;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserOptionResponse> getAllUsers() {
        List<UserOptionResponse> users = userRepository.findAll().stream()
                .map(u -> UserOptionResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .build())
                .toList();
        log.debug("Loaded {} users for selection", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public List<UserOptionResponse> getPhotographers() {
        List<UserOptionResponse> photographers = userRepository.findAll().stream()
                .filter(UserService::isPhotographer)
                .map(u -> UserOptionResponse.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .build())
                .toList();
        log.debug("Loaded {} photographers for selection", photographers.size());
        return photographers;
    }

    private static boolean isPhotographer(User user) {
        return user.getAuthorities() != null && user.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getRole()));
    }
}
