package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class ServiciosUsuario {
    private static ServiciosUsuario instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private Usuario usuario;
    private Usuario usuarioAct;
    public void SetUsuario (Usuario usuario) {
        this.usuario = usuario;
    }


    public Usuario GetUsuario () {
        return this.usuario;
    }

    public void SetUsuarioAct (Usuario usuario) {
        this.usuario = usuario;
    }


    public Usuario GetUsuarioAct () {
        return this.usuario;
    }



    private ServiciosUsuario() {
        // Constructor privado para evitar la creación de instancias fuera de la clase
        this.usuario = null;
    }

    public static ServiciosUsuario getInstance() {
        if (instancia == null) {
            instancia = new ServiciosUsuario();
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




    // *** FUNCIONES *** //
    // CREAR USUARIO -------------------------------------------------------------------------------------------------
    public Usuario CrearUsuario( String username, String nombre, String password, boolean administrador, boolean autor ){

        Usuario nuevoUsuario = null;
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear un nuevo usuario
            Usuario usuario = new Usuario(username, nombre, password, administrador, autor);
            nuevoUsuario = usuario;
            // Guardar el usuario en la base de datos
            session.save(usuario);

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

        return nuevoUsuario;

    }






    public Usuario login( String username, String password) {
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los usuarios
            List<Usuario> usuarios = session.createQuery("FROM Usuario", Usuario.class).list();

            // Revisar los usuarios
            for (Usuario usuario : usuarios) {
                if(username.equals(usuario.getUsername()) && password.equals(usuario.getPassword()))
                    return usuario;
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

        return null;
    }








    // *** UTILIDAD *** //
    // IMPRIMIR USUARIOS -------------------------------------------------------------------------------------------------
    public void imprimirUsuarios() {
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los usuarios
            List<Usuario> usuarios = session.createQuery("FROM Usuario", Usuario.class).list();

            // Imprimir los usuarios
            for (Usuario usuario : usuarios) {
                System.out.println(usuario.getId() + "  " + usuario.getUsername());
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



    public Usuario buscarUsuarioPorId( long id) {
        // Configurar y construir la sesión de Hibernate
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        Session session = null;
        Transaction transaction = null;

        try {
            // Abrir una sesión de Hibernate
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Crear consulta HQL para obtener todos los usuarios
            List<Usuario> usuarios = session.createQuery("FROM Usuario", Usuario.class).list();

            // Revidar Usuarios
            for (Usuario usuario : usuarios) {
                if(id == usuario.getId())
                    return usuario;
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

        return null;
    }


}



