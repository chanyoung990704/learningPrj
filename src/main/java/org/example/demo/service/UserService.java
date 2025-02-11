package org.example.demo.service;

import org.example.demo.domain.User;

public interface UserService extends BaseService<User> {
    User findByEmail(String email);
}
