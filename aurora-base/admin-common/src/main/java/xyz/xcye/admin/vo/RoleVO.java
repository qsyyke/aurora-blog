package xyz.xcye.admin.vo;

import lombok.Data;
import xyz.xcye.core.constant.FieldLengthConstant;
import xyz.xcye.core.valid.Delete;
import xyz.xcye.core.valid.Insert;
import xyz.xcye.core.valid.Update;
import xyz.xcye.core.valid.validator.ValidateString;

import javax.validation.constraints.NotNull;

@Data
public class RoleVO {

    /**
     * 唯一uid，自增
     */
    @NotNull(groups = {Delete.class, Update.class})
    private Long uid;

    /**
     * 角色的名称，不用添加ROLE_
     */
    @ValidateString(value = "角色的名称", max = FieldLengthConstant.USER_ROLE, groups = {Insert.class})
    private String name;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 最后更新时间
     */
    private String updateTime;

    /**
     * 用户的状态 1：已禁用 0：未禁用
     */
    private Boolean status;
}