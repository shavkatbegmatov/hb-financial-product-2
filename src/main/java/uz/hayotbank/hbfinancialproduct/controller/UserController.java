package uz.hayotbank.hbfinancialproduct.controller;

import uz.hayotbank.hbfinancialproduct.dto.UserCreateDto;
import uz.hayotbank.hbfinancialproduct.dto.UserResponseDto;
import uz.hayotbank.hbfinancialproduct.exception.UserNotFoundException;
import uz.hayotbank.hbfinancialproduct.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto user = userService.createUser(userCreateDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        UserResponseDto user = userService.getUserByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("username", username));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
        @RequestParam String searchTerm,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.searchUsers(searchTerm, pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
        @PathVariable Long id,
        @Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto user = userService.updateUser(id, userCreateDto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}