package org.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String titulo;

    private String cuerpo;

    private Long autorId;

    private Date fecha;

    public Articulo() {
    }


    public Articulo( String tituloPass, String cuerpo, long autor, Date fecha) {
        this.titulo = tituloPass;
        this.cuerpo = cuerpo;
        this.autorId = autor;
        this.fecha = fecha;

    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public long getAutor() {
        return autorId;
    }

    public void setAutor(long autor) {
        this.autorId = autor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
