package com.gimnasio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class Reserva {

    private int id;

    @JsonProperty("id_clase")
    private int idClase;

    private String socio;

    private LocalDate fecha;

    public Reserva() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdClase() {
        return idClase;
    }

    public void setIdClase(int idClase) {
        this.idClase = idClase;
    }

    public String getSocio() {
        return socio;
    }

    public void setSocio(String socio) {
        this.socio = socio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
