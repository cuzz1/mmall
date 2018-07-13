package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService{
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServiceResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }


    public ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) {
            // 这个产品不在这购物车里
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);

        } else {
            // 这个产品已经在购物车里
            // 如果产品已经存在数量添加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }







    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存充足的时候
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }







//    private CartVo getCartVoLimit(Integer userId) {
//        CartVo cartVo = new CartVo();
//        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
//        List<CartProductVo> cartProductVoList = Lists.newArrayList();
//        BigDecimal cartTotalPrice = new BigDecimal("0");
//
//        if (CollectionUtils.isNotEmpty(cartList)) {
//            for (Cart cartItem : cartList) {
//                CartProductVo cartProductVo = new CartProductVo();
//                cartProductVo.setId(cartItem.getId());
//                cartProductVo.setUserId(userId);
//                cartProductVo.setProductId(cartItem.getProductId());
//                Product product = productMapper.selectByPrimaryKey(cartItem.getId());
//                if (product != null) {
//                    cartProductVo.setProductMainImage(product.getMainImage());
//                    cartProductVo.setProductName(product.getName());
//                    cartProductVo.setProductSubtitle(product.getSubtitle());
//                    cartProductVo.setProductStatus(product.getStatus());
//                    cartProductVo.setProductPrice(product.getPrice());
//                    cartProductVo.setProductStock(product.getStock());
//                    // 判断库存
//                    int buyLimitCount = 0;
//                    if (product.getStock() >= cartItem.getQuantity()) {
//                        // 库存充足的时候
//                        buyLimitCount = cartItem.getQuantity();
//                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
//                    }else {
//                        buyLimitCount = product.getStock();
//                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
//                        // 购物车的数量为最大库存
//                        Cart cart = new Cart();
//                        cart.setId(cartItem.getId());
//                        cart.setQuantity(buyLimitCount);
//                        cartMapper.updateByPrimaryKeySelective(cart);
//                    }
//
//                    cartProductVo.setQuantity(buyLimitCount);
//                    // 计算总价
//                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
//                    cartProductVo.setProductChecked(cartItem.getChecked());
//                }
//
//                // 如果勾选了 添加到整个购物车的总价中
//                if (cartItem.getChecked() == Const.Cart.CHECKED) {
//                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
//                }
//                cartProductVoList.add(cartProductVo);
//            }
//        }
//        cartVo.setCartTotalPrice(cartTotalPrice);
//        cartVo.setCartProductVoList(cartProductVoList);
//        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
//        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
//        return cartVo;
//    }

    private Boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }
}
