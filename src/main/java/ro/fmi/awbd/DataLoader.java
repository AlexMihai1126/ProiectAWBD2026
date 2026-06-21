package ro.fmi.awbd;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.fmi.awbd.model.entity.security.Authority;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.repository.security.AuthorityRepository;
import ro.fmi.awbd.repository.security.UserRepository;

@AllArgsConstructor
@Component
@Profile({"test", "dev"})
@Slf4j
public class DataLoader implements CommandLineRunner {

    private AuthorityRepository authorityRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private void loadUserData() {
        if (userRepository.count() == 0) {
            Authority adminRole = authorityRepository.save(Authority.builder().role("ROLE_ADMIN").build());
            Authority guestRole = authorityRepository.save(Authority.builder().role("ROLE_GUEST").build());

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .authority(adminRole)
                    .build();

            User guest = User.builder()
                    .username("guest")
                    .password(passwordEncoder.encode("guest"))
                    .authority(guestRole)
                    .build();

            userRepository.save(admin);
            userRepository.save(guest);
            log.info("Seeded default users: admin (ADMIN), guest (GUEST)");
        } else {
            log.debug("User seed skipped, {} users already in database", userRepository.count());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }
}


