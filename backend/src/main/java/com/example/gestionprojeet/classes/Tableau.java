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
@Builder
public class Tableau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long idTableau ;
    private  String nom ;
    private  String  description ;
    @ManyToOne
    @JsonIgnore
    private Utlisateur proprietaire;
    @OneToMany(mappedBy = "tableau")
    @JsonIgnore
    private List<Projet> projets;

}
