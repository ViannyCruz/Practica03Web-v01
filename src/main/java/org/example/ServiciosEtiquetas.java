package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ServiciosEtiquetas {
    private static ServiciosEtiquetas instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private Etiqueta etiqueta;

    public void setEtiqueta(Etiqueta etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Etiqueta getEtiqueta() {
        return this.etiqueta;
    }

    private ServiciosEtiquetas() {
        // Constructor privado para evitar la creación de instancias fuera de la clase
        this.etiqueta = null;
    }

    public static ServiciosEtiquetas getInstance() {
        if (instancia == null) {
            instancia = new ServiciosEtiquetas();
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

    // CREAR ETIQUETA -------------------------------------------------------------------------------------------------
    public Etiqueta crearEtiqueta( String etiqueta) {
        Etiqueta nuevaEtiqueta = null;
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear una nueva etiqueta
            Etiqueta etiquetaObj = new Etiqueta( etiqueta);
            nuevaEtiqueta = etiquetaObj;

            // Guardar la etiqueta en la base de datos
            session.save(etiquetaObj);

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

        return nuevaEtiqueta;
    }

    // IMPRIMIR ETIQUETAS -------------------------------------------------------------------------------------------------
    public void imprimirEtiquetas() {
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todas las etiquetas
            List<Etiqueta> etiquetas = session.createQuery("FROM Etiqueta", Etiqueta.class).list();

            // Imprimir las etiquetas
            for (Etiqueta etiqueta : etiquetas) {
                System.out.println("ID: " + etiqueta.getId());
                System.out.println("Nombre: " + etiqueta.getEtiqueta());
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
            // Cerrar la sesión factory al finalizar la aplicación
            sessionFactory.close();
        }
    }

    // Método para obtener todas las etiquetas y devolverlas como un ArrayList
    public List<Etiqueta> obtenerTodasLasEtiquetas() {
        Session session = null;
        Transaction transaction = null;
        List<Etiqueta> etiquetas = new ArrayList<>();

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todas las etiquetas
            etiquetas = session.createQuery("FROM Etiqueta", Etiqueta.class).list();

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

        return etiquetas;
    }
    public Etiqueta getEtiquetaPorId(long id) {
        Session session = null;
        Transaction transaction = null;
        Etiqueta etiqueta = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Obtener la etiqueta por su ID
            etiqueta = session.get(Etiqueta.class, id);

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

        return etiqueta;
    }

    public Etiqueta etiquetaExiste(String nombreEtiqueta) {
        Session session = null;
        Transaction transaction = null;
        Etiqueta etiqueta = null;

        try {
            // Abrir una sesión de Hibernate desde la SessionFactory existente
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear una consulta HQL para obtener la etiqueta si existe
            List<Etiqueta> etiquetas = session.createQuery("FROM Etiqueta WHERE etiqueta = :nombreEtiqueta", Etiqueta.class)
                    .setParameter("nombreEtiqueta", nombreEtiqueta)
                    .list();

            // Verificar si se encontró una etiqueta
            if (!etiquetas.isEmpty()) {
                etiqueta = etiquetas.get(0); // Tomar la primera etiqueta encontrada
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

        return etiqueta;
    }



}
