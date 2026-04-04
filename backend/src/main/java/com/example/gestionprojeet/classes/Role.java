package com.example.gestionprojeet.classes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    ADMINISTRATEUR,
    MEMBRE,
    OBSERVATEUR;
    public static Role getDefaultRole() {
        return MEMBRE; // Rôle par défaut est MEMBRE
    }
    }

