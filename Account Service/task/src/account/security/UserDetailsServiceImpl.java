package account.security;

import account.model.event.Action;
import account.model.user.User;
import account.repository.UserRepository;
import account.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EventService eventService;
    private final HttpServletRequest request;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  EventService eventService,
                                  HttpServletRequest request) {
        this.userRepository = userRepository;
        this.eventService = eventService;
        this.request = request;
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        Optional<User> user = userRepository.findByEmailIgnoreCase(username);
        return user.map(UserDetailsImpl::new)
                .orElseThrow(() -> {
                    String path = request.getRequestURI().substring(request.getContextPath().length());
                    eventService.saveLog(Action.LOGIN_FAILED, username, path, path);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User doesn't exist");
                });
    }
}
