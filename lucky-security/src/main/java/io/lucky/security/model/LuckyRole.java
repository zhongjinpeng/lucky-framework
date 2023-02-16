package io.lucky.security.model;

import org.springframework.security.core.GrantedAuthority;

public class LuckyRole implements GrantedAuthority {

    private Long roleId;

    private String roleName;

    private String roleCode;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Override
    public String getAuthority() {
        return this.roleCode;
    }
}
