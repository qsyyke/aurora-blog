package xyz.xcye.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import xyz.xcye.admin.dao.PermissionRelationDao;
import xyz.xcye.admin.dto.RolePermissionDTO;
import xyz.xcye.admin.po.Role;
import xyz.xcye.admin.po.RolePermissionRelationship;
import xyz.xcye.admin.po.UserRoleRelationship;
import xyz.xcye.admin.service.*;
import xyz.xcye.admin.vo.UserVO;
import xyz.xcye.core.enums.ResponseStatusCodeEnum;
import xyz.xcye.core.exception.permission.PermissionException;
import xyz.xcye.core.exception.role.RoleException;
import xyz.xcye.core.exception.user.UserException;
import xyz.xcye.core.util.lambda.AssertUtils;
import xyz.xcye.data.entity.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qsyyke
 * @date Created in 2022/5/4 22:53
 */

@Service
public class PermissionRelationServiceImpl implements PermissionRelationService {

    private final String rolePrefix = "ROLE_";

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;
    @Autowired
    private RolePermissionRelationshipService rolePermissionRelationshipService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionRelationDao permissionRelationDao;

    @Override
    public List<RolePermissionDTO> loadPermissionByUserUid(long userUid) {
        return packageCollectResult(permissionRelationDao.loadPermissionByUserUid(userUid));
    }

    @Override
    public List<Role> loadAllRoleByUsername(String username) {
        UserVO userVO = userService.queryUserByUsername(username);
        AssertUtils.stateThrow(username != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));
        return permissionRelationDao.loadAllRoleByUserUid(userVO.getUid()).stream()
                .peek(role -> role.setName(rolePrefix + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionDTO> loadPermissionByUsername(String username) {
        UserVO userVO = userService.queryUserByUsername(username);
        if (userVO == null) {
            return new ArrayList<>();
        }
        return packageCollectResult(permissionRelationDao.loadPermissionByUserUid(userVO.getUid()));
    }

    @Override
    public List<RolePermissionDTO> loadPermissionByRoleName(String roleName) {
        return packageCollectResult(permissionRelationDao.loadPermissionByRoleName(roleName));
    }

    @Override
    public List<RolePermissionDTO> loadAllRolePermission(Condition<Long> condition) {
        return packageCollectResult(permissionRelationDao.loadAllRolePermission(condition));
    }

    @Override
    public List<RolePermissionDTO> queryRoleByPermissionPath(String permissionPath) {
        return packageCollectResult(permissionRelationDao.queryRoleByPermissionPath(permissionPath));
    }

    @Transactional
    @Override
    public int insertUserRoleBatch(long[] userUidArr, long roleUid) {
        final int[] successNum = {0};
        Role role = roleService.selectByUid(roleUid);
        // ??????????????????????????????
        AssertUtils.stateThrow(role != null,
                () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));

        // ??????????????????????????????
        AssertUtils.stateThrow(!role.getStatus(), () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_HAD_DISABLED));

        Arrays.stream(userUidArr)
                .filter(userUid -> userService.queryUserByUid(userUid) != null)
                .filter(userUid -> userRoleRelationshipService.selectAllUserRoleRelationship(Condition.instant(userUid, roleUid)).isEmpty())
                .forEach(userUid -> {
                    UserRoleRelationship userRoleRelationship = UserRoleRelationship.builder()
                            .roleUid(roleUid).userUid(userUid)
                            .build();
                    successNum[0] = successNum[0] + userRoleRelationshipService.insertUserRoleRelationship(userRoleRelationship);
                });
        return successNum[0];
    }

    @Override
    public int deleteUserRoleBatch(long userUid, long[] roleUidArr) {
        final int[] successNum = {0};
        // ????????????????????????
        AssertUtils.stateThrow(userService.queryUserByUid(userUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));

        // ??????????????????
        Condition<Long> condition = new Condition<>();
        condition.setUid(userUid);
        Arrays.stream(roleUidArr).forEach(roleUid -> {
            condition.setOtherUid(roleUid);
            // ??????roleUid???userUid?????????uid
            List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                    .selectAllUserRoleRelationship(condition);
            if (userRoleRelationshipList.size() > 0) {
                successNum[0] = successNum[0] + userRoleRelationshipService.deleteByUid(userRoleRelationshipList.get(0).getUid());
            }
        });
        return successNum[0];
    }

    @Override
    public int updateUserRole(long userUid, long originRoleUid, long newRoleUid) {
        final int[] successNum = {0};
        // ????????????????????????
        AssertUtils.stateThrow(userService.queryUserByUid(userUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_USER_NOT_EXIST));
        // ????????????????????????
        Condition<Long> condition = new Condition<>();
        condition.setUid(userUid);
        condition.setOtherUid(originRoleUid);

        List<UserRoleRelationship> userRoleRelationshipList = userRoleRelationshipService
                .selectAllUserRoleRelationship(condition);
        if (userRoleRelationshipList.size() > 0) {
            // ??????newRoleUid????????????
            AssertUtils.stateThrow(roleService.selectByUid(newRoleUid) != null,
                    () -> new RoleException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
            // ??????
            userRoleRelationshipList.get(0).setRoleUid(newRoleUid);
            successNum[0] = userRoleRelationshipService.updateUserRoleRelationship(userRoleRelationshipList.get(0));
        }

        return successNum[0];
    }

    @Override
    public int insertRolePermissionBatch(long[] roleUidArr, long permissionUid) {
        final int[] successNum = {0};

        // ?????????permissionUid????????????
        AssertUtils.stateThrow(permissionService.selectByUid(permissionUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));
        Assert.notNull(roleUidArr, "???????????????uid?????????null");
        // ???????????????roleUid
        Arrays.stream(roleUidArr)
                .filter(roleUid -> roleService.selectByUid(roleUid) != null)
                .filter(roleUid -> rolePermissionRelationshipService.selectAllRolePermissionRelationship(Condition.instant(roleUid, permissionUid)).isEmpty())
                .forEach(roleUid -> {
                    // ????????????????????????
                    RolePermissionRelationship rolePermissionRelationship = RolePermissionRelationship.builder()
                            .permissionUid(permissionUid).roleUid(roleUid)
                            .build();
                    // ????????????????????????
                    successNum[0] = successNum[0] + rolePermissionRelationshipService
                            .insertRolePermissionRelationship(rolePermissionRelationship);
                });
        return successNum[0];
    }

    @Override
    public int deleteRolePermissionBatch(long roleUid, long[] permissionUidArr) {
        final int[] successNum = {0};
        // ????????????????????????
        AssertUtils.stateThrow(roleService.selectByUid(roleUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
        // ??????????????????
        Condition<Long> condition = new Condition<>();
        condition.setUid(roleUid);
        Arrays.stream(permissionUidArr).forEach(permissionUid -> {
            // ?????????roleUid???permissionUid?????????uid
            condition.setOtherUid(permissionUid);
            List<RolePermissionRelationship> rolePermissionRelationshipList = rolePermissionRelationshipService
                    .selectAllRolePermissionRelationship(condition);
            if (rolePermissionRelationshipList.size() > 0) {
                // ??????
                successNum[0] = successNum[0] + rolePermissionRelationshipService
                        .deleteByUid(rolePermissionRelationshipList.get(0).getUid());
            }
        });

        return successNum[0];
    }

    @Override
    public int updateRolePermission(long roleUid, long originPermissionUid, long newPermissionUid) {
        final int[] successNum = {0};
        // ????????????????????????
        AssertUtils.stateThrow(roleService.selectByUid(roleUid) != null,
                () -> new UserException(ResponseStatusCodeEnum.PERMISSION_ROLE_NOT_EXISTS));
        // ????????????????????????
        Condition<Long> condition = new Condition<>();
        condition.setUid(roleUid);
        condition.setOtherUid(originPermissionUid);
        List<RolePermissionRelationship> permissionRelationshipList = rolePermissionRelationshipService
                .selectAllRolePermissionRelationship(condition);
        if (permissionRelationshipList.size() > 0) {
            // ?????????newPermissionUid????????????
            AssertUtils.stateThrow(permissionService.selectByUid(newPermissionUid) != null,
                    () -> new PermissionException(ResponseStatusCodeEnum.PERMISSION_RESOURCE_NOT_RIGHT));
            // ??????
            permissionRelationshipList.get(0).setPermissionUid(newPermissionUid);
            successNum[0] = rolePermissionRelationshipService.updateRolePermissionRelationship(permissionRelationshipList.get(0));
        }
        return successNum[0];
    }

    /**
     * ?????????????????????????????????????????????????????????ROLE_?????????????????????????????????????????????????????????????????????
     * @param rolePermissionDTOList
     * @return
     */
    private List<RolePermissionDTO> packageCollectResult(List<RolePermissionDTO> rolePermissionDTOList) {
        return rolePermissionDTOList.stream()
                .peek(rolePermissionDTO -> rolePermissionDTO.setRoleName(rolePrefix + rolePermissionDTO.getRoleName()))
                .collect(Collectors.toList());
    }
}
