package ru.skypro.marketplace.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.skypro.marketplace.entity.Role;
import ru.skypro.marketplace.entity.User;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SecurityUser implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + getRole());
        return Collections.singleton(authority);

    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static SecurityUser from(User user) {
        return new SecurityUser(user.getId(), user.getEmail(), user.getPassword(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getRole());
    }
}
