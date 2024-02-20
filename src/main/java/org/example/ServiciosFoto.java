package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class ServiciosFoto {
    private String FotoActual;
    public void setFoto(String foto){
        FotoActual = foto;

    }

    public String getFoto(){
        return FotoActual;

    }

    private static ServiciosFoto instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private ServiciosFoto() {
    }

    public static ServiciosFoto getInstance() {
        if (instancia == null) {
            instancia = new ServiciosFoto();
        }
        return instancia;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public void guardarFoto(String base64Image, long idUser) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            Foto foto = new Foto();
            foto.setBase64Image(base64Image);
            foto.setIdUser(idUser);
            session.save(foto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Foto> obtenerTodasLasFotos() {
        Session session = null;
        List<Foto> fotos = null;

        try {
            session = sessionFactory.openSession();
            fotos = session.createQuery("FROM Foto", Foto.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return fotos;
    }

    public String obtenerFotoUsuarioPorId(long idUsuario) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Consulta HQL para obtener la foto del usuario por su ID
            Foto foto = session.createQuery("FROM Foto WHERE idUser = :userId", Foto.class)
                    .setParameter("userId", idUsuario)
                    .uniqueResult();
            if (foto != null) {
                return foto.getBase64Image();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
