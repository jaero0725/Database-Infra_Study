package kr.ac.zeroco.ch04_shopping.domain.user.service;

import kr.ac.zeroco.ch04_shopping.domain.user.entity.User;
import kr.ac.zeroco.ch04_shopping.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
