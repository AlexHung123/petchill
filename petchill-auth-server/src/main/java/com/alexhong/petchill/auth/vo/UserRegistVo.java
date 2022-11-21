package com.alexhong.petchill.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {

    @NotEmpty(message = "User Name cannot be empty")
    @Length(min = 6, max = 18, message = "Should be around 6 and 18 characters")
    private String userName;

    @NotEmpty(message = "Password cannot be empty")
    @Length(min = 6, max = 18, message = "Should be around 6 and 18 characters")
    private String password;

    @NotEmpty(message = "Phone cannot be empty")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "Wrong format of phone number")
    private String phone;

    @NotEmpty(message = "Code cannot be empty")
    private String code;
}
