package com.example.gestionprojeet.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Carte {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long idCarte;

        private String titre;
        private String description;

        @Temporal(TemporalType.TIMESTAMP)
        private Date dateCreation;

        @Temporal(TemporalType.TIMESTAMP)
        private Date dateEcheance;

        @Enumerated(EnumType.STRING)
        private StatutCarte statut = StatutCarte.A_FAIRE;

        @ManyToOne
        @JsonIgnore
        private Projet projet;
        @ManyToOne
        @JoinColumn(name = "auteur_id")
        @JsonIgnoreProperties({"tokens", "tableaux", "projets", "forgotPassword", "password",
                "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "username"})
        private Utlisateur auteur;
        @OneToMany(mappedBy = "carte")
        @JsonIgnore
        private List<Commentaire> commentaires;
    }

