package ch.zhaw.freelancer4u.service;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    public boolean hasRole(String role, @AuthenticationPrincipal Jwt jwt) {
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");
        if (userRoles.contains(role)) {
            return true;
        }
        return false;
    }
}
