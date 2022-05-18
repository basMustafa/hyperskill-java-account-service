package account.service;

import account.dto.user.StatusResponseDTO;
import account.dto.user.ChangeAccessDTO;
import account.dto.user.RoleDTO;
import account.exception.UserExistException;
import account.exception.UserNotFoundException;
import account.model.event.Action;
import account.model.user.AccessOperation;
import account.model.user.Role;
import account.model.user.RoleOperation;
import account.model.user.User;
import account.repository.UserRepository;
import account.util.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class UserService {

    private final static Set<Role> BUSINESS_GROUP = Set.of(Role.USER, Role.ACCOUNTANT, Role.AUDITOR);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventService eventService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EventService eventService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventService = eventService;
    }

    public User findUserByEmail(String email) throws ResponseStatusException {
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public User registerUser(User user) {
        if (userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new UserExistException();
        }

        user.getRoles().add(userRepository.findAll().isEmpty() ? Role.ADMINISTRATOR : Role.USER);
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        eventService.saveLog(Action.CREATE_USER, "Anonymous", user.getEmail(), "/api/auth/signup");

        return userRepository.save(user);
    }

    public void changePassword(String email, String newPassword) {
        User user = findUserByEmail(email);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        eventService.saveLog(Action.CHANGE_PASSWORD, user.getEmail(), user.getEmail(), "/api/auth/changepass");
        userRepository.save(user);
    }

    public List<User> getAllUsersAndInfo() {
        return userRepository.findAll();
    }

    public void deleteUser(String userEmail, String adminEmail) {
        User user = findUserByEmail(userEmail);

        if (user.getRoles().contains(Role.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }

        userRepository.delete(user);
        eventService.saveLog(Action.DELETE_USER, adminEmail, userEmail, "/api/admin/delete");
    }

    public User updateUserRole(RoleDTO roleDTO, String adminEmail) throws ResponseStatusException {
        User user = findUserByEmail(roleDTO.getUser());

        Role role = AppUtils.valueOf(Role.class, roleDTO.getRole());
        RoleOperation operation = AppUtils.valueOf(RoleOperation.class, roleDTO.getOperation());
        String invalidReason = null;

        if (operation.equals(RoleOperation.GRANT) && isCombiningRoles(user.getRoles(), role)) {
            invalidReason = "The user cannot combine administrative and business roles!";
        } else if (operation.equals(RoleOperation.GRANT) && user.getRoles().contains(role)) {
            invalidReason = "User already has the role";
        } else if (operation.equals(RoleOperation.REMOVE) && role.equals(Role.ADMINISTRATOR)) {
            invalidReason = "Can't remove ADMINISTRATOR role!";
        } else if (operation.equals(RoleOperation.REMOVE) && !user.getRoles().contains(role)) {
            invalidReason = "The user does not have a role!";
        } else if (operation.equals(RoleOperation.REMOVE) && user.getRoles().size() == 1) {
            invalidReason = "The user must have at least one role!";
        }

        if (invalidReason != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidReason);
        }

        Action action;
        String fromOrTo;

        if (operation.equals(RoleOperation.GRANT)) {
            user.getRoles().add(role);
            action = Action.GRANT_ROLE;
            fromOrTo = "to";
        } else {
            user.getRoles().remove(role);
            action = Action.REMOVE_ROLE;
            fromOrTo = "from";
        }

        eventService.saveLog(action, adminEmail,
                String.format("%s role %s %s %s", StringUtils.capitalize(roleDTO.getOperation().toLowerCase()),
                        role.name(), fromOrTo, user.getEmail()),"/api/admin/role");

        return userRepository.save(user);
    }

    public boolean isCombiningRoles(Set<Role> assignedRoles, Role requestedRole) {
        return adminWithBusinessRole(assignedRoles, requestedRole) ||
                businessRoleWithAdmin(assignedRoles, requestedRole);
    }

    public boolean adminWithBusinessRole(Set<Role> assignedRoles, Role requestedRole) {
        return !Collections.disjoint(assignedRoles, BUSINESS_GROUP)
                && requestedRole.equals(Role.ADMINISTRATOR);
    }

    public boolean businessRoleWithAdmin(Set<Role> assignedRoles, Role requestedRole) {
        return assignedRoles.contains(Role.ADMINISTRATOR) && BUSINESS_GROUP.contains(requestedRole);
    }

    public StatusResponseDTO changeAccess(ChangeAccessDTO accessDTO, String adminEmail) {
        Action action;
        User user = findUserByEmail(accessDTO.getUser());
        AccessOperation op = AppUtils.valueOf(AccessOperation.class, accessDTO.getOperation());

        if (user.getRoles().contains(Role.ADMINISTRATOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }

        if (Objects.equals(op, AccessOperation.LOCK)) {
            user.setAccountNonLocked(false);
            action = Action.LOCK_USER;
        } else {
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            action = Action.UNLOCK_USER;
        }

        userRepository.save(user);
        eventService.saveLog(action, adminEmail,
                String.format("%s user %s", StringUtils.capitalize(op.name().toLowerCase()),
                        user.getEmail()), "/api/admin/access");

        return new StatusResponseDTO(String.format("User %s %sed!", user.getEmail(), op.name().toLowerCase()));
    }
}
