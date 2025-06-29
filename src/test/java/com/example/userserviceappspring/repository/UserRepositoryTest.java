package com.example.userserviceappspring.repository;


import com.example.userserviceappspring.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        savedUser = userRepository.save(user);
    }

    @Test
    void shouldSaveUserWithAuditFields() {
        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertEquals("Test User", savedUser.getName());
    }

    @Test
    void shouldFindUserById() {
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void shouldReturnAllUsers() {
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser.setAge(30);
        userRepository.save(anotherUser);

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void shouldUpdateUser() {
        savedUser.setName("Updated Name");
        User updatedUser = userRepository.save(savedUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(savedUser.getCreatedAt(), updatedUser.getCreatedAt());
    }

    @Test
    void shouldDeleteUser() {
        userRepository.deleteById(savedUser.getId());
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    void shouldNotUpdateCreatedAtOnModify() {
        LocalDateTime originalCreation = savedUser.getCreatedAt();

        savedUser.setName("Modified");
        userRepository.save(savedUser);

        User updatedUser = userRepository.findById(savedUser.getId()).get();
        assertEquals(originalCreation, updatedUser.getCreatedAt());
    }
}