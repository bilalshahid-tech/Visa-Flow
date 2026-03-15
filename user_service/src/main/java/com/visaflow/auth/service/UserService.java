package com.visaflow.auth.service;

import com.visaflow.auth.dto.UserDTO;
import com.visaflow.auth.model.User;
import com.visaflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public UserDTO updateProfile(User user, UserDTO updateRequest) {
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        
        User savedUser = userRepository.save(user);
        return authenticationService.convertToDTO(savedUser);
    }
}
