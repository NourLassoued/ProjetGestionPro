package com.example.gestionprojeet.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
public enum StatutCarte {
    A_FAIRE,
    EN_COURS,
    TERMINE
}
