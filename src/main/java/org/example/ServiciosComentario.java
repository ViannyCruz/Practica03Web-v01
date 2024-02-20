package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ServiciosComentario {
    private static ServiciosComentario instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private Comentario comentario;

    public void setComentario(Comentario comentario) {
        this.comentario = comentario;
    }

    public Comentario getComentario() {
        return this.comentario;
    }

    private ServiciosComentario() {
        // Constructor privado para evitar la creación de instancias fuera de la clase
        this.comentario = null;
    }

    public static ServiciosComentario getInstance() {
        if (instancia == null) {
            instancia = new ServiciosComentario();
        }
        return instancia;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            // Crear la SessionFactory a partir del archivo de configuración hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // En caso de error, imprimir el mensaje y lanzar una excepción
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // CREAR COMENTARIO -------------------------------------------------------------------------------------------------
// CREAR COMENTARIO -------------------------------------------------------------------------------------------------
    public Comentario crearComentario(String comentario, long autor, String autorName, long idArticulo) {
        Comentario nuevoComentario = null;
        // Configurar y construir la sesión de Hibernate
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear un nuevo comentario
            Comentario comentarioObj = new Comentario(comentario, autor, autorName, idArticulo);
            nuevoComentario = comentarioObj;

            // Guardar el comentario en la base de datos
            session.save(comentarioObj);

            // Confirmar la transacción
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                // Cerrar la sesión de Hibernate
                session.close();
            }
        }

        return nuevoComentario;
    }


    // IMPRIMIR COMENTARIOS -------------------------------------------------------------------------------------------------
    public void imprimirComentarios() {
        // Configurar y construir la sesión de Hibernate
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los comentarios
            List<Comentario> comentarios = session.createQuery("FROM Comentario", Comentario.class).list();

            // Imprimir los comentarios
            for (Comentario comentario : comentarios) {
                System.out.println("ID: " + comentario.getId());
                System.out.println("Contenido: " + comentario.getComentario());
                System.out.println("------------------------------------------");
            }

            // Confirmar la transacción
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                // Cerrar la sesión de Hibernate
                session.close();
            }
        }
    }

    // Método para obtener todos los comentarios y devolverlos como un ArrayList
    public List<Comentario> obtenerTodosLosComentarios() {
        Session session = null;
        Transaction transaction = null;
        List<Comentario> comentarios = new ArrayList<>();

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los comentarios
            comentarios = session.createQuery("FROM Comentario", Comentario.class).list();

            // Confirmar la transacción
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                // Cerrar la sesión de Hibernate
                session.close();
            }
        }

        return comentarios;
    }

    // Método para obtener un comentario por su ID
    public Comentario obtenerComentarioPorId(long id) {
        Session session = null;
        Transaction transaction = null;
        Comentario comentario = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Obtener el comentario por su ID
            comentario = session.get(Comentario.class, id);

            // Confirmar la transacción
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                // Cerrar la sesión de Hibernate
                session.close();
            }
        }

        return comentario;
    }
}
