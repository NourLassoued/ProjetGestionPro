package com.example.gestionprojeet.classes;

import com.example.gestionprojeet.Token.Token;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class Utlisateur  implements  UserDetails  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    private String firstname;

    private String email;
    private String password;
    private String image;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user")
    @JsonIgnore

    private ForgotPassword forgotPassword;
@JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)

private  List<Token> tokens;

    @OneToMany(mappedBy = "proprietaire")
    private List<Tableau> tableaux;
@JsonIgnore
@ManyToMany
@JoinTable(
        name = "utilisateur_projet",
        joinColumns = @JoinColumn(name = "idUtilisateur"),
        inverseJoinColumns = @JoinColumn(name = "idProjet")
)
private List<Projet> projets = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "auteur")
    private List<Carte> cartes;
    @OneToMany(mappedBy = "expediteur", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Message> messagesEnvoyes;
    @JsonIgnore

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return  List.of(new SimpleGrantedAuthority(role.name()));
    }
@JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }
@JsonIgnore
    @Override
    public String getUsername() {
        return email;
    }
@JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
@JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
@JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
@JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
