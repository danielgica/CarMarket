package com.proyecto.carmarket.Objetos;

import java.io.File;
import java.util.List;

public class Anuncio {
    private String id;
    private String annoMatriculacion;
    private String kilometraje;
    private String localidad;
    private String marca;
    private String modelo;
    private String nPlazas;
    private String potencia;
    private String precio;
    private String tipoCombustible;
    private String propietario;
    private List<String> fotos;
    private String fotoPrincipal;

    public Anuncio(String id, String annoMatriculacion, String kilometraje, String localidad,
                   String marca, String modelo, String nPlazas, String potencia,
                   String precio, String tipoCombustible, String propietario, List<String> fotos, String fotoPrincipal) {
        this.id = id;
        this.annoMatriculacion = annoMatriculacion;
        this.kilometraje = kilometraje;
        this.localidad = localidad;
        this.marca = marca;
        this.modelo = modelo;
        this.nPlazas = nPlazas;
        this.potencia = potencia;
        this.precio = precio;
        this.tipoCombustible = tipoCombustible;
        this.propietario = propietario;
        this.fotos = fotos;
        this.fotoPrincipal =fotoPrincipal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnnoMatriculacion() {
        return annoMatriculacion;
    }

    public void setAnnoMatriculacion(String annoMatriculacion) {
        this.annoMatriculacion = annoMatriculacion;
    }

    public String getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(String kilometraje) {
        this.kilometraje = kilometraje;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNPlazas() {
        return nPlazas;
    }

    public void setNPlazas(String nPlazas) {
        this.nPlazas = nPlazas;
    }

    public String getPotencia() {
        return potencia;
    }

    public void setPotencia(String potencia) {
        this.potencia = potencia;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }

    public void setTipoCombustible(String tipoCombustible) {
        this.tipoCombustible = tipoCombustible;
    }

    public String getPropietario() {
        return propietario;
    }

    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getFotoPrincipal() {
        return fotoPrincipal;
    }

    public void setFotoPrincipal(String fotoPrincipal) {
        this.fotoPrincipal = fotoPrincipal;
    }
}

