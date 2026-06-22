package ro.fmi.awbd.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ro.fmi.awbd.model.entity.security.Authority;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameReturnsSpringUserDetails() {
        Authority adminRole = Authority.builder().role("ROLE_ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("admin")
                .password("secret")
                .authority(adminRole)
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("secret");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsernameHandlesNullAuthorities() {
        User user = new User();
        user.setUsername("client");
        user.setPassword("pwd");
        user.setAuthorities(null);
        when(userRepository.findByUsername("client")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("client");

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsernameHandlesEmptyAuthorities() {
        User user = User.builder()
                .username("empty")
                .password("pwd")
                .build();
        when(userRepository.findByUsername("empty")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("empty");

        assertThat(details.getAuthorities()).isEmpty();
    }
}
