package com.lovePower.butWho.controller.user;

import com.lovePower.butWho.domain.user.User;
import com.lovePower.butWho.dto.UserDto;
import com.lovePower.butWho.dto.response.user.LoginResponse;
import com.lovePower.butWho.service.user.CustomUserDetailsService;
import com.lovePower.butWho.service.user.UserService;
import com.lovePower.butWho.util.JwtUtil;
import com.lovePower.butWho.domain.user.UserRepository;
import com.lovePower.butWho.dto.request.user.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) throws BadCredentialsException {
        if(userRepository.existsByEmail(userDto.getEmail())) {
            throw new BadCredentialsException("이미 존재하는 사용자 정보입니다.");
        }

        userService.createUser(userDto);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("옳지 않은 이메일 혹은 비밀번호입니다.", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(userDto.getEmail());

        String email = userDto.getEmail();
        User user = userRepository.findByEmail(email);

        final String jwt = jwtUtil.generateToken(userDetails, user.getAuthority());

        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("옳지 않은 이메일 혹은 비밀번호입니다.", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(loginRequest.getEmail());

        String email = loginRequest.getEmail();
        User user = userRepository.findByEmail(email);

        final String jwt = jwtUtil.generateToken(userDetails, user.getAuthority());

        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/result")
    public ResponseEntity<?> helloUser() {
        return  ResponseEntity.ok().build();
    }
}
