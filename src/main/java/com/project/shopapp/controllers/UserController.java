package com.project.shopapp.controllers;

import com.project.shopapp.component.JwtTokenUtil;
import com.project.shopapp.component.LocalizationUtils;
import com.project.shopapp.dtos.*;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.LoginResponse;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.responses.TokenResponse;
import com.project.shopapp.responses.UserResponse;
import com.project.shopapp.services.IUserService;
import com.project.shopapp.utils.MessageKeys;
import com.project.shopapp.utils.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final LocalizationUtils localizationUtils;
    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest()
                        .body(ResponseObject.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .message(errorMessages.toString())
                                .build());
            }
            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()){
                if(userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank()){
                    return ResponseEntity.badRequest().body(
                            ResponseObject.builder().status(HttpStatus.BAD_REQUEST)
                                    .data(null)
                                    .message("At least email or phone number is required")
                                    .build());
                } else {
                    //phone number not blank
                    if(!ValidationUtils.isValidPhoneNumber(userDTO.getPhoneNumber())){
                        throw new Exception("Invalid phone number");
                    }
                }
            } else {
                //Email not blank
                if(!ValidationUtils.isValidEmail(userDTO.getEmail())){
                    throw new Exception("Invalid email format");
                }
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest()
                        .body(ResponseObject.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                .build());
            }
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok().body(ResponseObject.builder().status(HttpStatus.CREATED)
                            .data(UserResponse.fromUser(user))
                            .message("Accout registration successful")
                    .build());
        }catch(Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(null).message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
//            @Valid
            @RequestBody UserLoginDTO userLoginDTO){
        try {
            String token = userService.login(userLoginDTO.getPhone_number(), userLoginDTO.getPassword());
            TokenResponse tokenResponse = new TokenResponse(token);
            System.out.println("Login Successfully");
            System.out.println(tokenResponse.getToken());
            return ResponseEntity.ok().body(tokenResponse);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvalidParamException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
        //Lay token tu header Authorization
        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Bearer ")){
            return ResponseEntity.badRequest().body("Token invalid or not have in request.");
        }
        token = token.substring(7); //Get token follow "Bearer ";
        //Call service to handle logout
        String reponse = userService.logout(token);
        return ResponseEntity.ok(reponse);
    }

    @GetMapping("/verify-token")
    public ResponseEntity<?> veryfyToken(@RequestHeader("Authorization") String token){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Token invalid");
        }

        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        //Kiem tra token
            String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
            User user = userService.findByPhoneNumber(phoneNumber);
            UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                    .phone_number(user.getPhoneNumber())
                    .password("******")
                    .build();
            return ResponseEntity.ok(userLoginDTO);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("email") String email){
        boolean exists = userService.verifyEmail(email);
        return ResponseEntity.ok(exists ? "exists" : "available");
    }

    @GetMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(@RequestParam("phone") String phone){
        boolean exists = userService.verifyPhone(phone);
        return ResponseEntity.ok(exists ? "exists" : "available");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam("to") String to){
        String otp = userService.generateOTP();
        boolean result = userService.sendEmail(to, otp);
        if(result){
            return ResponseEntity.ok().body(otp);
        }
        return (ResponseEntity<String>) ResponseEntity.badRequest();
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @Valid
            @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        System.out.println(userUpdatePasswordDTO.toString());
        boolean result = userService.updatePassword(userUpdatePasswordDTO);
        if (result) {
            return ResponseEntity.ok("Password updated successfully!");
        } else {
            return ResponseEntity.badRequest().body("User with provided email not found!");
        }
    }

    @PostMapping("/login-social")
    private ResponseEntity<TokenResponse> loginSocial(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) throws Exception{
        try {
        // Gọi hàm loginSocial từ UserService cho đăng nhập mạng xã hội
        String token = userService.loginSocial(userLoginDTO);
        TokenResponse tokenResponse = new TokenResponse(token);
        System.out.println("Login Successfully");
        System.out.println(tokenResponse.getToken());
        return ResponseEntity.ok().body(tokenResponse);
        } catch (DataNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvalidParamException e) {
            throw new RuntimeException(e);
        }
    }
}
