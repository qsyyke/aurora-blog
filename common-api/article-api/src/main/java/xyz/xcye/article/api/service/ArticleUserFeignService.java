package xyz.xcye.article.api.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import xyz.xcye.admin.po.User;
import xyz.xcye.core.entity.R;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.message.po.Email;

/**
 * @author qsyyke
 */

@FeignClient(value = "aurora-admin", name = "aurora-admin", contextId = "article-user-feign", fallback = ArticleUserFeignHandler.class)
public interface ArticleUserFeignService {

    @GetMapping("/admin/user/userUid/{uid}")
    R queryUserByUid(@PathVariable("uid") long uid);

    @PutMapping("/admin/user")
    R updateUser(@SpringQueryMap User user) throws UserException;

    @PutMapping("/admin/user/bindingEmail")
    R bindingEmail(@SpringQueryMap Email email) throws BindException;
}