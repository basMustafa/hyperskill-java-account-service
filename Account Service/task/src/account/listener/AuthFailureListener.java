package account.listener;

import account.model.event.Action;
import account.service.EventService;
import account.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import javax.servlet.http.HttpServletRequest;

@Configuration
public class AuthFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final LoginAttemptService loginAttemptService;
    private final EventService eventService;
    private final HttpServletRequest request;

    @Autowired
    public AuthFailureListener(LoginAttemptService loginAttemptService,
                               EventService eventService,
                               HttpServletRequest request) {
        this.loginAttemptService = loginAttemptService;
        this.eventService = eventService;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        eventService.saveLog(Action.LOGIN_FAILED, event.getAuthentication().getName(),
                request.getRequestURI(), request.getRequestURI());

        String username = event.getAuthentication().getName();
        loginAttemptService.loginFailure(username, request.getRequestURI());
    }
}
