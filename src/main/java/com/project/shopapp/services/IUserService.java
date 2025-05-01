package com.project.shopapp.services;

import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.dtos.UserUpdatePasswordDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.User;


public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password) throws DataNotFoundException, InvalidParamException;
    String logout(String token);
    boolean verifyToken(String token);
    User findByPhoneNumber(String phoneNumber);
    boolean verifyEmail(String email);
    boolean verifyPhone(String phone);
    String generateOTP();
    boolean sendEmail(String to, String code);
    boolean updatePassword(UserUpdatePasswordDTO userUpdatePasswordDTO);
    String loginSocial(UserLoginDTO userLoginDTO) throws Exception;
    User getUserDetailsFromToken(String token) throws  Exception;
}
