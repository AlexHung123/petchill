package com.alexhong.petchill.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class bank {
    public Long account_number;
    public String address;
    public Integer age;
    public BigDecimal balance;
    public String city;
    public String email;
    public String employer;
    public String firstname;
    public String gender;
    public String lastname;
    public String state;
}