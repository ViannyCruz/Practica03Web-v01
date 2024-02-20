package org.example;

import java.util.ArrayList;
import java.util.List;

import org.example.ArticuloEtiqueta;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class ServiciosArticuloEtiqueta {
    private static ServiciosArticuloEtiqueta instancia;
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private ServiciosArticuloEtiqueta() {}

    public static ServiciosArticuloEtiqueta getInstance() {
        if (instancia == null) {
            instancia = new ServiciosArticuloEtiqueta();
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

    // Función para almacenar un objeto ArticuloEtiqueta en la base de datos
    public void almacenarArticuloEtiqueta(ArticuloEtiqueta articuloEtiqueta) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.save(articuloEtiqueta);
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

    // Método para obtener todos los objetos ArticuloEtiqueta de la base de datos
    public List<ArticuloEtiqueta> obtenerTodosLosArticulosEtiquetas() {
        List<ArticuloEtiqueta> articulosEtiquetas = new ArrayList<>();
        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Realizar la consulta para obtener todos los objetos ArticuloEtiqueta
            articulosEtiquetas = session.createQuery("FROM ArticuloEtiqueta").list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return articulosEtiquetas;
    }

    public void imprimirArticulosEtiquetas() {
        List<ArticuloEtiqueta> articulosEtiquetas = obtenerTodosLosArticulosEtiquetas();

        if (articulosEtiquetas.isEmpty()) {
            System.out.println("No hay artículos con etiquetas para imprimir.");
        } else {
            System.out.println("Lista de Artículos con Etiquetas:");
            for (ArticuloEtiqueta articuloEtiqueta : articulosEtiquetas) {
                System.out.println(articuloEtiqueta.getIdArticulo()); // Aquí imprimirá los detalles de cada ArticuloEtiqueta
            }
        }
    }



    // Nueva función para obtener los objetos ArticuloEtiqueta por ID de Artículo
    public List<ArticuloEtiqueta> obtenerArticulosEtiquetaPorIdArticulo(long idArticulo) {
        List<ArticuloEtiqueta> articulosEtiquetas = new ArrayList<>();
        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Realizar la consulta para obtener los objetos ArticuloEtiqueta por ID de Artículo
            articulosEtiquetas = session.createQuery("FROM ArticuloEtiqueta WHERE idArticulo = :idArticulo")
                    .setParameter("idArticulo", idArticulo)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return articulosEtiquetas;
    }


    public List<ArticuloEtiqueta> getListaArticuloByEtiqueta(String etiqueta) {
        List<ArticuloEtiqueta> articulosEtiquetados = new ArrayList<>();
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            // Realizar la consulta para obtener los objetos ArticuloEtiqueta por etiqueta
            List<Etiqueta> etiquetas = session.createQuery("FROM Etiqueta WHERE etiqueta = :etiqueta", Etiqueta.class)
                    .setParameter("etiqueta", etiqueta)
                    .list();

            for (Etiqueta etiquetaObj : etiquetas) {
                // Para cada etiqueta, obtenemos los ArticuloEtiqueta asociados
                List<ArticuloEtiqueta> articulos = session.createQuery("FROM ArticuloEtiqueta WHERE etiqueta = :etiqueta", ArticuloEtiqueta.class)
                        .setParameter("etiqueta", etiquetaObj)
                        .list();
                // Agregamos los resultados a la lista principal
                articulosEtiquetados.addAll(articulos);
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return articulosEtiquetados;
    }


    public void borrarArticuloEtiquetaPorIdArticulo(long idArticulo) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Realizar la consulta para obtener los objetos ArticuloEtiqueta por ID de Artículo
            List<ArticuloEtiqueta> articulosEtiquetas = session.createQuery("FROM ArticuloEtiqueta WHERE idArticulo = :idArticulo")
                    .setParameter("idArticulo", idArticulo)
                    .list();

            // Eliminar todos los registros de ArticuloEtiqueta asociados al ID de artículo
            for (ArticuloEtiqueta articuloEtiqueta : articulosEtiquetas) {
                session.delete(articuloEtiqueta);
            }

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

}
