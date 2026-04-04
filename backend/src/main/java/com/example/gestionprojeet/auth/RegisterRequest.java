package com.example.gestionprojeet.auth;

import com.example.gestionprojeet.classes.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest implements Serializable {
    private  String firstname;

    private String email;
    private String password;
private String image;
private  Role role;

}
