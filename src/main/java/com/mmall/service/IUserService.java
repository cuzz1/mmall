package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

public interface IUserService {
    ServiceResponse<User> login(String username, String password);

    ServiceResponse<String> register(User user);

    ServiceResponse<String> checkValid(String str, String data);

    ServiceResponse<String> selectQuestion(String username);

    ServiceResponse<String> checkAnswer(String username, String question, String answer);

    ServiceResponse<String> forgetResetpassword(String username, String newPassword, String forgetToken);

    public ServiceResponse<String> resetPassword(String oldPassword, String newPassowrd, User user);

    public ServiceResponse<User> updateInformation(User user);

    public ServiceResponse<User> getInformation(Integer userId);

    public ServiceResponse checkAdminRole(User user);
}

