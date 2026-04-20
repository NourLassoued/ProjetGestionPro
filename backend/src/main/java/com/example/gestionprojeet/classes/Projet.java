package com.example.gestionprojeet.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
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
    @OneToMany(mappedBy = "projet", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Carte> cartes = new ArrayList<>();
    @ManyToMany(mappedBy = "projets")
    @JsonIgnore
    private List<Utlisateur> utilisateurs = new ArrayList<>();


}