package com.aust.its.service;

import com.aust.its.dto.RegisterPayload;
import com.aust.its.entity.HelpDeskUser;
import com.aust.its.entity.User;
import com.aust.its.exception.UserAlreadyExistsException;
import com.aust.its.exception.UserNotFoundException;
import com.aust.its.repository.UserRepository;
import com.aust.its.utils.Commons;
import com.aust.its.utils.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final HelpDeskUserService helpDeskUserService;
    private final PasswordEncoder customPasswordEncoder;

    public User getById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }


    public HelpDeskUser register(RegisterPayload payload) {
        Optional<User> userOptional = userRepository.findById(payload.userId());

        if(userOptional.isEmpty()) {
            throw new UserNotFoundException("the user is not an iums user");
        }

        if(helpDeskUserService.isHelpDeskUserAlreadyExists(payload.userId())) {
            throw new UserAlreadyExistsException("the user is already exists in the helpdesk system");
        }

        User user = userOptional.get();
        HelpDeskUser helpDeskUserToSave = new HelpDeskUser();
        helpDeskUserToSave.setUserId(payload.userId());
        helpDeskUserToSave.setEmail(payload.email());
        helpDeskUserToSave.setPassword(customPasswordEncoder.encode(payload.password()));

        if(!Commons.isNullOrEmpty(user.getEmployeeId())) {
            helpDeskUserToSave.setRoleId(Const.Role.EMPLOYEE_ROLE_ID);
        } else {
            helpDeskUserToSave.setRoleId(Const.Role.STUDENT_ROLE_ID);
        }

        return helpDeskUserService.saveHelpDeskUser(helpDeskUserToSave);
    }
}
