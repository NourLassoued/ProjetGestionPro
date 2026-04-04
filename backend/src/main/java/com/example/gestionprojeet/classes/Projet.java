package com.example.gestionprojeet.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long idProjet   ;
    private String nom     ;
    private  String description;
    @ManyToOne
    @JsonIgnore
    private Tableau tableau;
    @OneToMany(mappedBy = "projet")
    @JsonIgnore
    private List<Carte> cartes;
    @ManyToMany(mappedBy = "projets")
    private List<Utlisateur> utilisateurs;


}
