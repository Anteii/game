package com.onlinegame.game.service;

import com.onlinegame.game.dto.UserForm;
import com.onlinegame.game.model.Role;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
public class UserService {
    private static final Integer USER_PROFILE_PICTURE_MAX_SIZE = 1024 * 1024 * 8;
    private static final String DEFAULT_USER_PROFILE_PICTURE_NAME = "default.jpg";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void activateUser(String username){
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setIsEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public User createNewUser(UserForm userForm, MultipartFile file){
        User user = new User();
        // Configure user
        user.setUsername(userForm.getUsername());
        user.setPassword(passwordEncoder.encode(userForm.getUsername()));
        user.setNickname(userForm.getNickname());
        user.setEmail(userForm.getEmail());
        user.setName(userForm.getName());
        user.setCreationDate(java.time.Instant.now());
        user.setWinedGames(0);
        user.setTotalGames(0);
        user.setScore(1000);
        user.setRole(Role.USER);
        user.setIsEnabled(false);
        user.setPosition(userRepository.maxPosition()+1);

        try {
            if (file.getSize() == 0) throw new IOException();
            //TODO Should do something to avoid this getNextIdCall;
            Integer id = userRepository.getNextId();
            // Configure path for profile image and save it
            String[] fileArr = StringUtils.cleanPath(file.getOriginalFilename()).split("\\.");
            String ext = fileArr[fileArr.length-1];
            String filename = id.toString() + "." + ext;

            FileService.saveFile(FileService.USERS_PROFILE_PICTURES_DIR, filename, file);
            user.setAvatarPic(filename);
        } catch (IOException e) {
            user.setAvatarPic(DEFAULT_USER_PROFILE_PICTURE_NAME);
        }

        userRepository.save(user);
        userRepository.flush();
        return user;
    }

    public boolean isFileSuitable(MultipartFile file){
        return file.getSize() < USER_PROFILE_PICTURE_MAX_SIZE;
    }
    public boolean isUsernameFree(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }
    public boolean isEmailFree(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }
}
