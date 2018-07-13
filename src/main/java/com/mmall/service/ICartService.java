package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServiceResponse<CartVo> list(Integer userId);
}
