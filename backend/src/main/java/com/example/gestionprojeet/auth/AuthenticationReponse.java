package com.example.gestionprojeet.auth;

import com.example.gestionprojeet.classes.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationReponse {
    @JsonProperty("access_token")
    private  String accesToken;
    @JsonProperty("refersh_token")
    private String refershToken;
    private Role role;
    private String image;
}
