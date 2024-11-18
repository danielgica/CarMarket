package com.proyecto.carmarket.Objetos;

import com.google.firebase.firestore.DocumentReference;

public class Marca {
    private String nombre;
    private String urlIcono;

    public Marca(String nombre, String urlIcono) {
        this.nombre = nombre;
        this.urlIcono = urlIcono;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUrlIcono() {
        return urlIcono;
    }
}
