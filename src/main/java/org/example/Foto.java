package org.example;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Foto {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String base64Image; // Almacena la imagen en formato base64
    private long idUser;
    public Foto() {}

    public Foto(String base64Image) {
        this.base64Image = base64Image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }
}
