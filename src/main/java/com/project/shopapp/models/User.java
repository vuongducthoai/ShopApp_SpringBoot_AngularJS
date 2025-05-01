package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
@Table(name = "users")
public class User extends BaseEntity  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="fullname", length = 100)
    private String fullName;

    @Column(name="phone_number", length = 10, nullable = false)
    private String phoneNumber;

    @Column(name = "email", length = 200, nullable = false)
    private String email;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "facebook_account_id")
    private String facebookAccountId;

    @Column(name = "google_account_id")
    private String googleAccountId;

    @ManyToOne
    @JoinColumn(name="role_id")
    private Role role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //Phuong thuc tra ve mot tap hop cac quyen (GrantedAuthority) cua nguoi dung hien tại
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+getRole().getName().toUpperCase()));
//            authorityList.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return authorityList;
    }

    @Override
    public String getUsername() { // Truong dang nhap
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() { // Kiem tra xem tai khoan nguoi dung co  bi het han hay khong
        return true; // Tai khoan van hop le, chua het han
        //return false; //Tai khoan da het han va khong the dang nhap duoc nua
    }

    @Override
    public boolean isAccountNonLocked() { //Kiem tra tai khoan co bi khoa hay khong
        return true; // Mac dinh tai khoan khong bi khoa
    }

    @Override
    public boolean isCredentialsNonExpired() { // Kiem tra thong tin dang nhap (mat khau) co het han khong
        return true; // Mat khau van con hop le
        //return false; Mat khau da het han, nguoi dung phai doi mat khau
    }

    @Override
    public boolean isEnabled() { // Kiem tra xem tai khoan co dc kich hoat hay khogn
        return true; // Tai khoan dang hoat dong
        //return false; Tai khoan bi vo hieu hoa
    }
}
