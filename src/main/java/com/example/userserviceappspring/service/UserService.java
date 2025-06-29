package com.example.userserviceappspring.service;


import com.example.userserviceappspring.event.UserEvent;
import com.example.userserviceappspring.event.UserEventType;
import com.example.userserviceappspring.exception.ResourceNotFoundException;
import com.example.userserviceappspring.model.User;
import com.example.userserviceappspring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final String TOPIC = "user-events";
    private final UserRepository userRepository;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;



    @Autowired
    public UserService(UserRepository userRepository, KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        sendUserEvent(UserEventType.CREATE, savedUser.getEmail());
        return savedUser;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setAge(userDetails.getAge());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        sendUserEvent(UserEventType.DELETE, user.getEmail());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    private void sendUserEvent(UserEventType eventType, String email) {
        UserEvent event = new UserEvent(eventType, email);
        kafkaTemplate.send(TOPIC, event);
    }

}