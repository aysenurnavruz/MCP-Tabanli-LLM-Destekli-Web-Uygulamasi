package com.elif.mcpproject.security;

import com.elif.mcpproject.user.AppUser;
import com.elif.mcpproject.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public AppUser requireCurrentUser(Principal principal){
        if (principal == null || principal.getName() == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"));
    }
}
