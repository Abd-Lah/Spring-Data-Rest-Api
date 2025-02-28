package com.spring.demo_relationship.controller.user;

import com.spring.demo_relationship.commands.DoctorProfileCommand;
import com.spring.demo_relationship.commands.UserCommand;
import com.spring.demo_relationship.dto.DoctorDto;
import com.spring.demo_relationship.mapper.DoctorMapper;
import com.spring.demo_relationship.models.Role;
import com.spring.demo_relationship.models.UserEntity;
import com.spring.demo_relationship.service.user.UserRoleMapperFactory;
import com.spring.demo_relationship.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRoleMapperFactory userRoleMapperFactory;

    @GetMapping("/user")
    public ResponseEntity<Object> user() {
        UserEntity user = userService.getCurrentUser();
        Object dto = userRoleMapperFactory.getMapper(user.getRole(), user);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all/{search}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<?>> users(@PathVariable String search, Pageable pageable) {
        Page<UserEntity> users = userService.getAllUsersByRole(search, pageable);
        Page<?> mappedPage = userRoleMapperFactory.getMapper(Role.valueOf(search.toUpperCase()), users);
        return ResponseEntity.ok(mappedPage);
    }

    @GetMapping("/all_doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<Page<DoctorDto>> doctors(Pageable pageable) {
        Page<UserEntity> users = userService.getAllUsersByRole("DOCTOR", pageable);
        return new ResponseEntity<>(DoctorMapper.INSTANCE.toDtoPage(users), HttpStatus.OK);
    }

    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<Page<DoctorDto>> doctor(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String specialization,
            Pageable pageable) {

        Page<UserEntity> usersPage = userService.getAllDoctors(firstName, lastName, city, specialization, pageable);

        return new ResponseEntity<>(DoctorMapper.INSTANCE.toDtoPage(usersPage), HttpStatus.OK);
    }


    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<DoctorDto> doctor(@PathVariable String id) {
        UserEntity user = userService.getDoctor(id);
        return new ResponseEntity<>(DoctorMapper.INSTANCE.toDto(user), HttpStatus.OK);
    }

    @PutMapping("/user/update_profile")
    public ResponseEntity<?> updateUser(@RequestBody UserCommand userCommand) {
        UserEntity currentUser = userService.getCurrentUser();

        UserEntity updatedUser = userService.updateProfile(userCommand);

        Object dto = userRoleMapperFactory.getMapper(currentUser.getRole(), updatedUser);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    @PutMapping("/user/update_doctor_profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorDto> updateUser(@RequestBody DoctorProfileCommand doctorProfileCommand) {
        return new ResponseEntity<>(DoctorMapper.INSTANCE.toDto(userService.updateProfile(doctorProfileCommand)),HttpStatus.OK);
    }

}
