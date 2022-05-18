package account.controller;

import account.dto.user.StatusResponseDTO;
import account.dto.user.ChangeAccessDTO;
import account.dto.user.DeleteUserResponseDTO;
import account.dto.user.RoleDTO;
import account.dto.user.UserDTO;
import account.mapper.ModelMapper;
import account.model.user.User;
import account.security.UserDetailsImpl;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public AdminController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/user")
    public List<UserDTO> showAllUserInfo() {
        return userService.getAllUsersAndInfo().stream()
                .map(modelMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/user/{email}")
    public DeleteUserResponseDTO deleteUser(@PathVariable String email,
                                            @AuthenticationPrincipal UserDetails userDetails) {

        userService.deleteUser(email, userDetails.getUsername());
        return new DeleteUserResponseDTO(email);
    }

    @PutMapping("/user/role")
    public UserDTO updateUserRole(@RequestBody RoleDTO roleDTO,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.updateUserRole(roleDTO, userDetails.getUsername());
        return modelMapper.mapToDTO(user);
    }

    @PutMapping("/user/access")
    public StatusResponseDTO changeUserAccess(@RequestBody ChangeAccessDTO accessDTO,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        return userService.changeAccess(accessDTO, userDetails.getUsername());
    }
}
