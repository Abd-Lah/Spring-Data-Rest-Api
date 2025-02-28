package com.spring.demo_relationship.service.user;

import com.spring.demo_relationship.commands.DoctorProfileCommand;
import com.spring.demo_relationship.commands.UserCommand;
import com.spring.demo_relationship.models.DoctorProfile;
import com.spring.demo_relationship.models.Role;
import com.spring.demo_relationship.models.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserEntity getCurrentUser();
    UserEntity updateProfile(UserCommand userCommand);
    UserEntity updateProfile(DoctorProfileCommand profile);
    Page<UserEntity> getAllUsersByRole(String role, Pageable pageable);

    Page<UserEntity> getAllDoctors(String firstName, String lastName, String city, String specialization, Pageable pageable);

    UserEntity getDoctor(String id);
}
