package com.project.shopapp.services.Impl;

import com.project.shopapp.component.JwtTokenUtil;
import com.project.shopapp.component.LocalizationUtils;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.dtos.UserUpdatePasswordDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.ExpiredTokenException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.exception.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.IUserService;
import com.project.shopapp.utils.MessageKeys;
import com.project.shopapp.utils.ValidationUtils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final LocalizationUtils localizationUtils;

    @Value("${spring.security.oath2.client.registration.google.client_id}")
    private String googleClientId;

    @Value("${app.email.username}")
    private String from;

    @Value("${app.email.password}")
    private String password;

    //Register
    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        String email = userDTO.getEmail();
        //Kiểm tra xem số điện thoa đã tồn tại chưa
        if(userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }

        if(userRepository.existsByEmail(email)) {
            throw new DataIntegrityViolationException("Email already exists");
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
                        .facebookAccountId(userDTO.getFacebookAccountId())
                        .googleAccountId(userDTO.getGoogleAccountId())
                        .email(userDTO.getEmail())
                        .build();
        newUser.setRole(role);

        //Kiem tra neu co accountId, khong yeu cau password
        if(userDTO.getFacebookAccountId().isEmpty() && userDTO.getGoogleAccountId().isEmpty()) {
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
        if(existingUser.getFacebookAccountId().isEmpty()
                && existingUser.getGoogleAccountId().isEmpty()) {
            if(!passwordEncoder.matches(password,
                    existingUser.getPassword())) {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        //Tao authentication token
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(
                phoneNumber, password, existingUser.getAuthorities()
        );
        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        //Tao JWT token
        String jwtToken = jwtTokenUtil.generateToken(existingUser);

        //Xac dinh ngay het han cua token (VD la 30 ngay)
        Date expirationDate = jwtTokenUtil.extractAllClaims(jwtToken).getExpiration(); //Lay expiration tu token

        //Luu token vao database
        Optional<Token> existingToken = tokenRepository.findByUser(existingUser);
        Token tokenToSave;
        if(existingToken.isPresent()){ //Neu token ton tai
            tokenToSave = existingToken.get();
            tokenToSave.setToken(jwtToken);
            tokenToSave.setExpirationDate(expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()); // convert date to local date time
            tokenToSave.setRevoked(false);
            tokenToSave.setExpired(false);
        } else { // Token not existing
            tokenToSave = Token.builder()
                          .token(jwtToken)
                          .tokenType("JWT")
                          .expirationDate(expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                          .revoked(false)
                          .expired(false)
                          .user(existingUser)
                          .build();
        }
        tokenRepository.save(tokenToSave);

        return jwtToken;
    }

    @Override
    public String logout(String token) {
        String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);

        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            return "User not found!";
        }

        User user  = optionalUser.get();
        Optional<Token> optionalToken = tokenRepository.findByUser(user);
        if(optionalToken.isEmpty()){
            return "Not found token with this user";
        }
        Token tokenToRevoke = optionalToken.get();
        tokenToRevoke.setRevoked(true);
        tokenToRevoke.setExpired(true);
        tokenRepository.save(tokenToRevoke);

       return "Log out successfully";
    }

    public boolean verifyToken(String token){
        return jwtTokenUtil.validateToken(token, null);
    }

    @Override
    public User findByPhoneNumber(String phoneNumber) {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if(user.isPresent()){
            return user.get();
        }
        return null;
    }

    @Override
    public boolean verifyEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean verifyPhone(String phone) {
        Optional<User> user = userRepository.findByPhoneNumber(phone);
        if(user.isPresent()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String generateOTP() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    @Override
    public boolean sendEmail(String to, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        };

        Session session = Session.getInstance(props, auth);
        Message msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject("Reset Password");
            String content = "<h1>Hello " + to + "</h1>" +
                    "<p>Vui lòng nhập mã: <b>" + code + "</b> để xác thực email.</p>";
            msg.setContent(content, "text/html; charset=utf-8");
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePassword(UserUpdatePasswordDTO userUpdatePasswordDTO) {
        try {
            User existingUser = userRepository.findByEmail(userUpdatePasswordDTO.getEmail()).orElseThrow(() -> new DataNotFoundException("Cannot find User with: "+ userUpdatePasswordDTO.getEmail()));
            if(existingUser.getFacebookAccountId().isEmpty() && existingUser.getGoogleAccountId().isEmpty()) {
                System.out.println(userUpdatePasswordDTO.getPassword());
                existingUser.setPassword(passwordEncoder.encode(userUpdatePasswordDTO.getPassword()));

                userRepository.save(existingUser);
                return  true;
            }

        } catch (DataNotFoundException e){
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public String loginSocial(UserLoginDTO userLoginDTO) throws  Exception {
        Optional<User> optionalUser = Optional.empty();
        Role roleUser = roleRepository.findByName(Role.USER).orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        // If not exist, create new user
        if(userLoginDTO.isGoogleAccountIdValid()){
            optionalUser = userRepository.findByGoogleAccountId(userLoginDTO.getGoogleAccountId());

            //Create new user if not found
            if(optionalUser.isEmpty()){
                User newUser = User.builder()
                        .fullName(Optional.ofNullable(userLoginDTO.getFullname()).orElse(""))
                        .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
                        .profileImage(Optional.ofNullable(userLoginDTO.getProfileImage()).orElse(""))
                        .role(roleUser)
                        .googleAccountId(userLoginDTO.getGoogleAccountId())
                        .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
                        .active(true)
                        .build();

                //Save newUser
                newUser = userRepository.save(newUser);
                optionalUser = Optional.of(newUser);

            } // Kiểm tra Facebook Account ID
        }
//        else if (userLoginDTO.isFacebookAccountIdValid()) {
//            optionalUser = userRepository.findByFacebookAccountId(userLoginDTO.getFacebookAccountId());
//
//            // Tạo người dùng mới nếu không tìm thấy
//            if (optionalUser.isEmpty()) {
//                User newUser = User.builder()
//                        .fullName(Optional.ofNullable(userLoginDTO.getFullname()).orElse(""))
//                        .email(Optional.ofNullable(userLoginDTO.getEmail()).orElse(""))
//                        .profileImage(Optional.ofNullable(userLoginDTO.getProfileImage()).orElse(""))
//                        .role(roleUser)
//                        .facebookAccountId(userLoginDTO.getFacebookAccountId())
//                        .password("") // Mật khẩu trống cho đăng nhập mạng xã hội
//                        .active(true)
//                        .build();
//
//                // Lưu người dùng mới
//                newUser = userRepository.save(newUser);
//                optionalUser = Optional.of(newUser);
//            }
//        }
        else {
            throw new IllegalArgumentException("Invalid social account information.");
        }

        User user = optionalUser.get();

        // Kiểm tra nếu tài khoản bị khóa
        if (!user.isActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }
        // Tạo JWT token cho người dùng
        return jwtTokenUtil.generateToken(user);
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtil.isTokenExpired(token)){
            throw new ExpiredTokenException("Token is expired");
        }
        String subject = jwtTokenUtil.extractPhoneNumber(token);
        Optional<User> user;
        user = userRepository.findByPhoneNumber(subject);
        if(user.isEmpty() && ValidationUtils.isValidEmail(subject)){
            user = userRepository.findByEmail(subject);
        }
        return user.orElseThrow(() -> new Exception("User not found"));
    }
}
