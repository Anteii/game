package com.onlinegame.game.service;

import com.onlinegame.game.dto.UserForm;
import com.onlinegame.game.model.Friendship;
import com.onlinegame.game.model.Role;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.FriendshipRepository;
import com.onlinegame.game.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
public class UserService {
    private static final Integer USER_PROFILE_PICTURE_MAX_SIZE = 1024 * 1024 * 8;
    private static final String DEFAULT_USER_PROFILE_PICTURE_NAME = "default.jpg";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendshipRepository friendshipRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.friendshipRepository = friendshipRepository;
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

    public List<User> getUserFriends(String username){
        User user = userRepository.findByUsername(username).orElseThrow();
        return getUserFriends(user);
    }
    @Transactional
    public List<User> getUserFriends(User user){
        return userRepository.getFriendsListById(user.getUserId());
    }
    public void deleteFriend(User user, String friendUsername){
        User friendToDelete = userRepository.findByUsername(friendUsername).orElseThrow();
        Friendship f1 = friendshipRepository.findByUserOneAndUserTwo(user, friendToDelete).orElseThrow();
        Friendship f2 = friendshipRepository.findByUserOneAndUserTwo(friendToDelete, user).orElseThrow();
        //friendshipRepository.delete(f1);
        friendshipRepository.deleteInBatch(List.of(f1, f2));
        //friendshipRepository.delete(f2);
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
    public boolean isCredentialsRight(String username, String password){
        try{
            User user = userRepository.findByUsername(username).orElseThrow();
            return user.getPassword().equals(password);
        } catch (Exception e){
            return false;
        }
    }
}
