package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加一个分类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServiceResponse addCategory(HttpSession session,
                                       String categoryName,
                                       @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登入");
        }
        // 检验一下是不是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServiceResponse.createByErrorMessage("你不是管理员");
    }

    /**
     * 更新类品名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServiceResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登入");
        }
        // 检验一下是不是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 是管理员
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return ServiceResponse.createByErrorMessage("你不是管理员");
    }

    /**
     * 获取子类不递归
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登入");
        }
        // 检验一下是不是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            // 查询子节点的category信息 并且不递归保持平级
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return ServiceResponse.createByErrorMessage("你不是管理员");
    }
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServiceResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登入");
        }
        // 检验一下是不是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }
        return ServiceResponse.createByErrorMessage("你不是管理员");
    }
}
