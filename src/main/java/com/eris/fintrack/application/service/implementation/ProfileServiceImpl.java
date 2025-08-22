package com.eris.fintrack.application.service.implementation;

import com.eris.fintrack.application.service.ProfileService;
import com.eris.fintrack.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserContextService userContextService;

    @Override
    public User getCurrentUserProfile(){
        return userContextService.getCurrentUser();
    }

}
