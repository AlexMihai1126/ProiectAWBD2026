package ro.fmi.awbd;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.fmi.awbd.model.entity.security.Authority;
import ro.fmi.awbd.model.entity.security.User;
import ro.fmi.awbd.model.entity.ClientEntity;
import ro.fmi.awbd.repository.ClientRepository;
import ro.fmi.awbd.repository.security.AuthorityRepository;
import ro.fmi.awbd.repository.security.UserRepository;

import java.util.HashSet;

@AllArgsConstructor
@Component
@Profile({"test", "dev"})
@Slf4j
public class DataLoader implements CommandLineRunner {

    private AuthorityRepository authorityRepository;
    private UserRepository userRepository;
    private ClientRepository clientRepository;
    private PasswordEncoder passwordEncoder;

    private void loadUserData() {
        Authority adminRole = findOrCreateRole("ROLE_ADMIN");
        Authority clientRole = findOrCreateRole("ROLE_CLIENT");

        ensureDefaultUser("admin", "admin", adminRole);
        User client = ensureDefaultUser("client", "client", clientRole);
        User client2 = ensureDefaultUser("client2", "client2", clientRole);
        User client3 = ensureDefaultUser("client3", "client3", clientRole);

        ensureClient("Demo Client", "client@example.com", client);
        ensureClient("Portrait Client", "client2@example.com", client2);
        ensureClient("Events Client", "client3@example.com", client3);
    }

    private Authority findOrCreateRole(String role) {
        return authorityRepository.findByRole(role)
                .orElseGet(() -> authorityRepository.save(Authority.builder().role(role).build()));
    }

    private User ensureDefaultUser(String username, String defaultPassword, Authority requiredRole) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            User saved = userRepository.save(User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(defaultPassword))
                    .authority(requiredRole)
                    .build());
            log.info("Seeded default user: {}", username);
            return saved;
        }

        boolean changed = false;
        if (!isBcryptHash(user.getPassword()) || !passwordEncoder.matches(defaultPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(defaultPassword));
            changed = true;
            log.info("Restored default development password for user: {}", username);
        }

        HashSet<Authority> authorities = new HashSet<>();
        if (user.getAuthorities() != null) {
            authorities.addAll(user.getAuthorities());
        }
        if (authorities.stream().noneMatch(a -> requiredRole.getRole().equals(a.getRole()))) {
            authorities.add(requiredRole);
            user.setAuthorities(authorities);
            changed = true;
            log.info("Assigned {} to user: {}", requiredRole.getRole(), username);
        }

        if (changed) {
            user = userRepository.save(user);
        }
        return user;
    }

    private void ensureClient(String name, String email, User user) {
        if (clientRepository.findByUserId(user.getId()).isEmpty()) {
            clientRepository.save(ClientEntity.builder()
                    .name(name)
                    .email(email)
                    .user(user)
                    .build());
            log.info("Seeded client profile for account: {}", user.getUsername());
        }
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }
}
