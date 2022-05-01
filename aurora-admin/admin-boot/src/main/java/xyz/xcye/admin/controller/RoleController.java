package xyz.xcye.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.xcye.admin.service.RoleService;
import xyz.xcye.core.annotaion.ResponseResult;
import xyz.xcye.core.dto.Condition;
import xyz.xcye.core.entity.result.ModifyResult;
import xyz.xcye.common.po.RoleDO;
import xyz.xcye.core.valid.Insert;
import xyz.xcye.core.valid.Update;
import xyz.xcye.common.vo.RoleVO;

import javax.validation.groups.Default;
import java.util.List;

/**
 * @author qsyyke
 */

@RequestMapping("/admin/role")
@RestController
@Api(tags = "角色相关的操作")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ResponseResult
    @ApiOperation(value = "插入角色")
    @PostMapping("")
    public ModifyResult insertRole(@Validated({Insert.class, Default.class}) RoleDO roleDO) {
        return roleService.insert(roleDO);
    }

    @ResponseResult
    @ApiOperation(value = "修改角色信息")
    @PutMapping("")
    public ModifyResult updateRole(@Validated({Update.class, Default.class}) RoleDO roleDO) {
        return roleService.update(roleDO);
    }

    @ResponseResult
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/{uid}")
    public ModifyResult deleteRole(@PathVariable("uid") int uid) {
        return roleService.deleteByUid(uid);
    }

    @ResponseResult
    @ApiOperation(value = "根据uid查询角色")
    @GetMapping("/{uid}")
    public RoleVO queryRoleByUid(@PathVariable("uid") int uid) throws ReflectiveOperationException {
        return roleService.queryByUid(uid);
    }

    @ResponseResult
    @ApiOperation(value = "查询满足要求的所有角色信息")
    @GetMapping("")
    public List<RoleVO> queryRoleByUid(Condition<Integer> condition) throws ReflectiveOperationException {
        return roleService.queryAllByCondition(condition);
    }
}
