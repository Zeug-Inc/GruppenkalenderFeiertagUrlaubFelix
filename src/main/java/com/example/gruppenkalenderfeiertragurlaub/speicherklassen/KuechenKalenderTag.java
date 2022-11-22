package com.example.gruppenkalenderfeiertragurlaub.speicherklassen;

import java.time.LocalDate;

public class KuechenKalenderTag extends TagBasisKlasse {
    private Integer kuecheCurrentlyGeoeffnet;
    private Boolean kuecheOriginallyGeoeffnet;

    public KuechenKalenderTag(LocalDate datum, Integer kuecheCurrentlyGeoeffnet) {
        this.datum = datum;
        this.kuecheCurrentlyGeoeffnet = kuecheCurrentlyGeoeffnet;
        this.kuecheOriginallyGeoeffnet = (kuecheCurrentlyGeoeffnet == 1);
    }

    public Integer getKuecheCurrentlyGeoeffnet() {
        return kuecheCurrentlyGeoeffnet;
    }

    public void setKuecheCurrentlyGeoeffnet(Integer kuecheCurrentlyGeoeffnet) {
        this.kuecheCurrentlyGeoeffnet = kuecheCurrentlyGeoeffnet;
    }

    public Boolean getKuecheOriginallyGeoeffnet() {
        return kuecheOriginallyGeoeffnet;
    }
}
