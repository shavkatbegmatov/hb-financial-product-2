package uz.hayotbank.hbfinancialproduct.controller;

import uz.hayotbank.hbfinancialproduct.dto.JwtResponseDto;
import uz.hayotbank.hbfinancialproduct.dto.LoginRequestDto;
import uz.hayotbank.hbfinancialproduct.dto.UserCreateDto;
import uz.hayotbank.hbfinancialproduct.dto.UserResponseDto;
import uz.hayotbank.hbfinancialproduct.entity.User;
import uz.hayotbank.hbfinancialproduct.security.JwtUtil;
import uz.hayotbank.hbfinancialproduct.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto user = userService.createUser(userCreateDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );

            String jwt = jwtUtil.generateJwtToken(loginRequest.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getUsername());

            User user = userService.findEntityByUsername(loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponseDto(jwt, refreshToken, user.getId(),
                    user.getUsername(), user.getEmail(), user.getFullName()));
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestParam String refreshToken) {
        if (jwtUtil.validateJwtToken(refreshToken)) {
            String username = jwtUtil.getUserNameFromJwtToken(refreshToken);
            String newAccessToken = jwtUtil.generateJwtToken(username);
            String newRefreshToken = jwtUtil.generateRefreshToken(username);

            User user = userService.findEntityByUsername(username);

            return ResponseEntity.ok(new JwtResponseDto(newAccessToken, newRefreshToken,
                    user.getId(), user.getUsername(), user.getEmail(), user.getFullName()));
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }
}