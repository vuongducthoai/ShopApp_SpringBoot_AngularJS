package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Date;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("phone_number")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;


    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    private String email;

    private String address;

    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Retype Password is required")
    @JsonProperty("retype_password")
    private String retypePassword;

    @JsonProperty("date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @JsonProperty("facebook_account_id")
    private String facebookAccountId;

    @JsonProperty("google_account_id")
    private String googleAccountId;

    @NotNull(message = "Role ID is required")
    @JsonProperty("role_id")
    private long roleId;
}
