package com.example.cvd.user;

import org.junit.jupiter.api.Test;

import com.example.cvd.entity.User;

import static org.junit.jupiter.api.Assertions.*;

// POJO test, not including Spring stuff (routes, etc.)
// Lets us know User Entity file's getters and setters work
class UserEntityTest {

    @Test
    void testGettersAndSetters() {
        // create new user object
        User u = new User();

        // setting values to test each field
        u.setId(23L);
        u.setUsername("lebron");
        u.setEmail("lebron@example.com");
        u.setPassword("password123");
        u.setTeam("cat");
        u.setAdmin(false);

        // testing getters
        assertEquals(23L, u.getId());
        assertEquals("lebron", u.getUsername());
        assertEquals("lebron@example.com", u.getEmail());
        assertEquals("password123", u.getPassword());
        assertEquals("cat", u.getTeam());
        assertFalse(u.getAdmin()); // because admin = false
    }

    // test that when user account is first getting created, it is null or will default to false after null
    @Test
    void testAdminDefaultsToNull() {
        // creating a new user without setting admin manually
        User u = new User();

        // verifying that admin is null before saving
        // Supebase will handle rest
        assertNull(u.getAdmin(), "admin should be null in Java before persistence; DB handles default false");
    }

    
}
