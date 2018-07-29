package com.mmall.service.Impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }

        // 密码登入MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5Password);

        if (user == null) {
            return ServiceResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);

        return ServiceResponse.createBySuccess("登入成功", user);
    }

    @Override
    public ServiceResponse<String> register(User user) {
        // 判断用户名是否存在
        int resultCount = userMapper.checkUsername(user.getUsername());
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("用户名已经存在");
        }

        // 判断邮箱是否存在
        resultCount = userMapper.checkEmail(user.getEmail());
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("邮箱已经存在");
        }

        // 设置用户角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        // MD5 加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        resultCount = userMapper.insert(user);

        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("注册失败");
        }

        return ServiceResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServiceResponse<String> checkValid(String str, String type) {
        if (isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                // 判断用户名是否存在
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("用户名已经存在");
                }
            }

            if (Const.EMAIL.equals(type)) {
                // 判断邮箱是否存在
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("邮箱已经存在");
                }
            }

        } else {
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功");
    }

    public ServiceResponse<String> selectQuestion(String username) {
        ServiceResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (isNotBlank(question)) {
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题是空");
    }

    public ServiceResponse<String> checkAnswer(String username, String qusetion, String answer) {
        int resultCount = userMapper.checkAnswer(username, qusetion, answer);
        if (resultCount > 0) {
            // 说明问题及问题答案是这个用户的并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }

        return ServiceResponse.createByErrorMessage("问题答案错误");
    }

    public ServiceResponse<String> forgetResetpassword(String username, String newPassword, String forgetToken) {
        if (isBlank(forgetToken)) {
            return ServiceResponse.createByErrorMessage("参数错误, 没有token");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (isBlank(token)) {
            return ServiceResponse.createByErrorMessage("token无效");
        }

        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);;

            if (rowCount > 0) {
                return ServiceResponse.createBySuccessMessage("修改成功");
            }
        } else {
            return ServiceResponse.createBySuccessMessage("token错误,请重新获取token");
        }

        return ServiceResponse.createByErrorMessage("修改密码失败");
    }

    public ServiceResponse<String> resetPassword(String oldPassword, String newPassowrd, User user) {
        // 防止横向越权 要校正一下这个用户的旧密码
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword), user.getId());
        if (resultCount == 0) {
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(newPassowrd));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServiceResponse.createBySuccessMessage("密码更新成功");
        }
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }

    public ServiceResponse<User> updateInformation(User user) {
        // username 是不能更新
        // email 也要进行检验新的email是不是已经存在的 并且email相同不能是当前用户的
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("email已经存在");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServiceResponse.createBySuccessMessage("更新个人信息成功");
        }

        return ServiceResponse.createByErrorMessage("更新个人信息失败");
    }


    public ServiceResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServiceResponse.createByErrorMessage("找不到当前用户");
        }
        // 把密码去掉
        user.setPassword(EMPTY);
        return ServiceResponse.createBySuccess(user);
    }


    // backend
    public ServiceResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }
}
