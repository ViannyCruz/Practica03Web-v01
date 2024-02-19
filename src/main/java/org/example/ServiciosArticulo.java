package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiciosArticulo {
    private static ServiciosArticulo instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Constructor privado para evitar instanciación directa
    private ServiciosArticulo() {}

    // Método estático para obtener la instancia Singleton
    public static ServiciosArticulo getInstance() {
        if (instancia == null) {
            instancia = new ServiciosArticulo();
        }
        return instancia;
    }

    // Método estático para construir la SessionFactory una sola vez
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

    // Método para guardar un artículo ya creado
    public Articulo guardarArticulo(Articulo articulo) {

        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Guardar el artículo en la base de datos
            session.saveOrUpdate(articulo);

            // Confirmar la transacción
            transaction.commit();
            return articulo;
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
        return null;
    }

    // Método para imprimir todos los artículos
    public void imprimirArticulos() {
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los artículos
            List<Articulo> articulos = session.createQuery("FROM Articulo", Articulo.class).list();

            // Imprimir los artículos
            for (Articulo articulo : articulos) {
                System.out.println(articulo.getId() + " - " + articulo.getTitulo());
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



    // Método para obtener todos los artículos y devolverlos como un ArrayList
    public List<Articulo> obtenerTodosLosArticulos() {
        Session session = null;
        Transaction transaction = null;
        List<Articulo> articulos = new ArrayList<>();

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los artículos
            articulos = session.createQuery("FROM Articulo", Articulo.class).list();

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

        return articulos;
    }


    public Articulo obtenerArticuloPorId(long id) {
        Session session = null;
        Transaction transaction = null;
        Articulo articulo = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Obtener el artículo por su ID utilizando el método get de Hibernate
            articulo = session.get(Articulo.class, id);

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

        return articulo;
    }

}
