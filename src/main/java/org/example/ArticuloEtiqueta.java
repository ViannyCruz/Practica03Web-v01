package org.example;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ArticuloEtiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private long idArticulo;
    private long idEtiqueta;

    // Constructor vacío necesario para JPA
    public ArticuloEtiqueta() {
    }

    // Constructor con todos los atributos
    public ArticuloEtiqueta( long idArticulo, long idEtiqueta) {
        this.idArticulo = idArticulo;
        this.idEtiqueta = idEtiqueta;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setIdArticulo(long idArticulo) {
        this.idArticulo = idArticulo;
    }

    public long getIdArticulo() {
        return idArticulo;
    }

    public void setIdEtiqueta(long idEtiqueta) {
        this.idEtiqueta = idEtiqueta;
    }

    public long getIdEtiqueta() {
        return idEtiqueta;
    }
}
