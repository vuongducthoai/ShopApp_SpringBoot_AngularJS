package com.project.shopapp.services.Impl;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException {
        String phoneNumber = userDTO.getPhoneNumber();
        //Kiểm tra xem số điện thoa đã tồn tại chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
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
        Role role =  roleRepository.findById(userDTO.getRoleId()).orElseThrow(() -> new DataNotFoundException("Role not found"));
        newUser.setRole(role);

        //Kiem tra neu co accountId, khong yeu cau password
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
        }
        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public String login(String phoneNumber, String password) {
        //Doan loen quan nhieu den security, se lam trong phan security
        return null;
    }
}
