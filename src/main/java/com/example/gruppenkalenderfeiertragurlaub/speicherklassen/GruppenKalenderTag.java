package com.example.gruppenkalenderfeiertragurlaub.speicherklassen;

import java.time.LocalDate;

public class GruppenKalenderTag {
    int gruppenID;
    LocalDate datum;
    char gruppenstatus;
    Boolean essenFuerGruppeVerfügbar;

    public GruppenKalenderTag(int gruppenID, LocalDate datum, char gruppenstatus, Boolean essenFuerGruppeVerfügbar) {
        this.gruppenID = gruppenID;
        this.datum = datum;
        this.gruppenstatus = gruppenstatus;
        this.essenFuerGruppeVerfügbar = essenFuerGruppeVerfügbar;
    }

    public int getGruppenID() {
        return gruppenID;
    }

    public void setGruppenID(int gruppenID) {
        this.gruppenID = gruppenID;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public char getGruppenstatus() {
        return gruppenstatus;
    }

    public void setGruppenstatus(char gruppenstatus) {
        this.gruppenstatus = gruppenstatus;
    }

    public Boolean getEssenFuerGruppeVerfügbar() {
        return essenFuerGruppeVerfügbar;
    }

    public void setEssenFuerGruppeVerfügbar(Boolean essenFuerGruppeVerfügbar) {
        this.essenFuerGruppeVerfügbar = essenFuerGruppeVerfügbar;
    }
}