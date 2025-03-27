package com.project.shopapp.services.Impl;

import com.project.shopapp.component.JwtTokenUtil;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.exception.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    //Register
    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        //Kiểm tra xem số điện thoa đã tồn tại chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role =  roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(()
                        -> new DataNotFoundException("Role not found"));

        if(role.getName().toUpperCase().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You cannot register admin account");
        }
        //Convert from UserDTO => user
        User newUser = User.builder()
                        .fullName(userDTO.getFullName())
                        .phoneNumber(userDTO.getPhoneNumber())
                        .password(userDTO.getPassword())
                        .address(userDTO.getAddress())
                        .dateOfBirth(userDTO.getDateOfBirth())
                        .facebookAccountId(userDTO.getFacebookAccountId())
                        .googleAccountId(userDTO.getGoogleAccountId())
                        .build();
        newUser.setRole(role);

        //Kiem tra neu co accountId, khong yeu cau password
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
    }

    //Login
    @Override
    public String login(String phoneNumber, String password) throws DataNotFoundException, InvalidParamException {
        Optional<User> optionalUser =  userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phoneNumber or password");
        }
        User existingUser = optionalUser.get();
        //check password
        if(existingUser.getFacebookAccountId() == 0
                && existingUser.getGoogleAccountId() == 0) {
            if(!passwordEncoder.matches(password,
                    existingUser.getPassword())) {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(
                phoneNumber, password, existingUser.getAuthorities()
        );
        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }
}
