package account.service;

import account.model.event.Action;
import account.model.user.Role;
import account.model.user.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private final static int MAX_ATTEMPT = 5;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Autowired
    public LoginAttemptService(UserRepository userRepository, EventService eventService) {
        super();
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public void loginSuccess(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    public void loginFailure(String key, String uri) {
        if (userRepository.findByEmailIgnoreCase(key).isEmpty()) {
            return;
        }

        User user = userRepository.findByEmailIgnoreCase(key).get();

        if (user.getRoles().contains(Role.ADMINISTRATOR)) {
            return;
        }

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= MAX_ATTEMPT) {
            user.setAccountNonLocked(false);
            eventService.saveLog(Action.BRUTE_FORCE, key, uri, uri);
            eventService.saveLog(Action.LOCK_USER, key, String.format("Lock user %s", key), uri);
        }

        userRepository.save(user);
    }
}
