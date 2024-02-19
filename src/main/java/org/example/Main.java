package org.example;

import io.javalin.Javalin;
import io.javalin.http.Cookie;
import io.javalin.http.staticfiles.Location;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.*;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.TextEncryptor;

public class Main {
    public static void main(String[] args) {

        /* SERVIDOR JAVALIN */
        // Iniciar el servidor de Javalin
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/publico";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = false;
                staticFileConfig.aliasCheck = null;
            });
        }).start(7000);

        // Ruta para la página principal
        app.get("/", ctx -> {
            // Si no hay un usuario logeado
            if (ServiciosUsuario.getInstance().login("TheAdmin", "123") == null) {
                ServiciosUsuario.getInstance().SetUsuario(ServiciosUsuario.getInstance().CrearUsuario("TheAdmin", "Admin", "123", true, false));
                ServiciosUsuario.getInstance().SetUsuarioAct(ServiciosUsuario.getInstance().login("TheAdmin", "123"));
                ctx.redirect("login.html");
            } else {
                ServiciosUsuario.getInstance().SetUsuario(ServiciosUsuario.getInstance().login("TheAdmin", "123"));
                ServiciosUsuario.getInstance().SetUsuarioAct(ServiciosUsuario.getInstance().login("TheAdmin", "123"));
                ctx.redirect("/login.html");
            }

        });


        /* INICIAR EL SERVIDOR HD */
        H2Server.getInstancia().init();

        /* CREAR UNA INSTANCIA DE UserAuthenticationLogger */
        UserAuthenticationLogger logger = new UserAuthenticationLogger();

        /* ENCRIPTACION */
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword("C&V");

        /** RECIBIDORES */
        // RECIBIDOR COOKIES
        app.post("/cookie", ctx -> {
            // Obtener el valor de las cookie
            String usernameCookie = ctx.cookie("usernameCookie");
            String passwordCookie = ctx.cookie("passwordCookie");
            Usuario u = ServiciosUsuario.getInstance().login(textEncryptor.decrypt(usernameCookie), textEncryptor.decrypt(passwordCookie));
            if (u != null) {
                System.out.println("galleta");


                // ** CUCARACHA ** //
                // Insertar un nuevo usuario en cockroach
                String nuevoUsuario = u.getUsername();
                logger.logUserAuthentication(nuevoUsuario);

                //test todo: remove
                logger.printAllAuthenticatedUsers();

                ctx.status(200);
            } else {
                ctx.status(404);
            }
        });


        // RECIBIDOR ADMIN
        // Manejar la solicitud GET para obtener los datos del usuario
        app.get("/obtenerAdmin", ctx -> {
            System.out.println("Entro a obtener admin");
            String usuarioJson = convertirAJson(ServiciosUsuario.getInstance().GetUsuario());
            ServiciosUsuario.getInstance().SetUsuario(null);

            // Enviar los datos del usuario al cliente
            ctx.json(usuarioJson);
        });

        // RECIBIDOR LOGIN
        app.post("/login", ctx -> {
            // Obtener los parámetros enviados en la solicitud POST
            String nombreUsuario = ctx.formParam("username");
            String contrasena = ctx.formParam("password");

            // Obtener el valor de la checkbox como una cadena
            String rememberMeStr = ctx.formParam("rememberMe");
            System.out.println(rememberMeStr);

            // Validar usuario
            Usuario user = ServiciosUsuario.getInstance().login(nombreUsuario, contrasena);

            if (user != null) {

                ServiciosUsuario.getInstance().SetUsuarioAct(user);

                //TODO: CUCARACHA

                // ** CUCARACHA ** //
                // Insertar un nuevo usuario en cockroach
                String nuevoUsuario = user.getUsername();
                logger.logUserAuthentication(nuevoUsuario);

                //test todo: remove
                logger.printAllAuthenticatedUsers();
                if (rememberMeStr.equals("true")) {

                    // COOKIES
                    // Cookie Username
                    String usernameEncripted = textEncryptor.encrypt(user.getUsername());
                    ctx.cookie("usernameCookie", usernameEncripted, 40);

                    // Cookie Password
                    String passwordEncripted = textEncryptor.encrypt(user.getPassword());
                    ctx.cookie("passwordCookie", passwordEncripted, 40);
                    System.out.println("Remenber me");  //todo REMOVE*/
                }
                ctx.status(200);
            } else {
                ctx.status(404);
            }

        });


        app.post("/crearArticulo", ctx -> {

            //FIXME: LA PUTAS ETIQUETAS NO SE ESTAN SEPARANDO BIEN

            // Obtener los parámetros enviados en la solicitud POST
            String titulo = ctx.formParam("titulo");
            String contenido = ctx.formParam("contenido");
            String etiqueta = ctx.formParam("etiqueta"); //String


            Date fecha = new Date();
            //TODO: INDICARLE AL MALDITO USUSAIRO COMO DEBE PONER LA ETIUQTE 1, 2,
            // PONER UN PUTO ", " AL INAL CUANDO EL ANIMAL, EL USUARIO, MANDE LA VAINA
            String[] etiquetaSplit = etiqueta.split(", ");

            Articulo newArticulo = new Articulo(titulo, contenido, ServiciosUsuario.getInstance().GetUsuarioAct().getId(), fecha);

            Articulo newArticuloTuti = ServiciosArticulo.getInstance().guardarArticulo(newArticulo);

            // Crear etiquetas con ID y palabra
            for (String palabra : etiquetaSplit) {
                Etiqueta etiquetaNew = new Etiqueta(palabra); //SI AQUI SE HACE CON SERVICO FUNCIONA
                if (ServiciosEtiquetas.getInstance().etiquetaExiste(etiquetaNew.getEtiqueta()) == null) {
                    //Crear nuevo articulo Etiqueta/
                    // SDFSDFSDFSDFSDFDSSDF
                    Etiqueta etiquetaNewTuti = ServiciosEtiquetas.getInstance().crearEtiqueta(palabra);
                    ArticuloEtiqueta artiEti = new ArticuloEtiqueta(newArticuloTuti.getId(), etiquetaNewTuti.getId());
                    ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(artiEti);
                } else {

                    //aqui hay que coger la etiqueta por id
                    Etiqueta barbie = ServiciosEtiquetas.getInstance().etiquetaExiste(palabra);
                    //Agregar etiqueta a base de datos
                    //ServiciosEtiquetas.getInstance().crearEtiqueta(palabra);
                    //Crear nuevo articulo Etiqueta
                    ArticuloEtiqueta artiEti = new ArticuloEtiqueta(newArticuloTuti.getId(), barbie.getId());
                    ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(artiEti);
                    System.out.println(" Tu maldita madre" + artiEti.getIdArticulo() +" "+ artiEti.getIdEtiqueta());

                    /*
                    //Agregar etiqueta a base de datos
                    ServiciosEtiquetas.getInstance().crearEtiqueta(palabra);
                    //Crear nuevo articulo Etiqueta
                    ArticuloEtiqueta artiEti = new ArticuloEtiqueta(newArticuloTuti.getId(), etiquetaNew.getId());
                    ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(artiEti);
                    System.out.println(" Tu maldita madre" + artiEti.getIdArticulo() +" "+ artiEti.getIdEtiqueta());
               */
                }
            }


            ServiciosArticulo.getInstance().imprimirArticulos();
            ServiciosArticuloEtiqueta.getInstance().imprimirArticulosEtiquetas();


        });


        // *** PRUEBAS *** //
        // TestUsuario
        // Obtener la instancia de ServiciosUsuario
        // ServiciosUsuario serviciosUsuario = ServiciosUsuario.getInstance();

        // Crear algunos usuarios para probar
        // serviciosUsuario.CrearUsuario("usuario1", "Usuario Uno", "123456", false, true);

        // Imprimir los usuarios existentes
        // System.out.println("Lista de usuarios existentes:");
        // serviciosUsuario.imprimirUsuarios();


        // TestEtiquetas
        // Obtener la instancia de ServiciosUsuario
        //   ServiciosEtiquetas serviciosEtiquetas = ServiciosEtiquetas.getInstance();

        // Crear algunas etiquetas para probar
        // ServiciosEtiquetas.getInstance().crearEtiqueta("Etiqueta1");
        // ServiciosEtiquetas.getInstance().crearEtiqueta("Etiqueta2");


        // System.out.println("Lista de etiquetas existentes:");
        // ServiciosEtiquetas.getInstance().imprimirEtiquetas();
        // System.out.println("\n");

        //Date date = new Date();
        //Articulo tuti = new Articulo("TutiArticulo", "hola", 1, date);

        //System.out.println(tuti.getTitulo() + "\n");
        //System.out.println("batata");

        //ServiciosArticulo.getInstance().guardarArticulo(tuti);
        // ServiciosArticulo.getInstance().imprimirArticulos();




        /* TESTY */
        /*
        // Crear etiqueta
        ServiciosEtiquetas.getInstance().crearEtiqueta("Etiqueta1");

        // Crear Articulo
        Date date = new Date();
        Articulo tuti = new Articulo("TutiArticulo", "hola", 1, date);
        tuti = ServiciosArticulo.getInstance().guardarArticulo(tuti);

        // Crear Articulo_Etiqueta
        ArticuloEtiqueta articuloEtiqueta = new ArticuloEtiqueta(tuti.getId(), 1);
        ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(articuloEtiqueta);


        // Crear lista de etiquetas que pertenezcan a un articulo
        List<ArticuloEtiqueta> listArticulosEtiquetas = ServiciosArticuloEtiqueta.getInstance().obtenerTodosLosArticulosEtiquetas();
        for (ArticuloEtiqueta aE : listArticulosEtiquetas) {
            System.out.println(aE.getIdArticulo());
        }

        // Buscar articulo por etiqueta
        // Agregar etiquetas que posean el mismo id al articulo
*/


        app.get("/cargarArticulos", ctx -> {
            List<Articulo> lista = ServiciosArticulo.getInstance().obtenerTodosLosArticulos();
            List<ArticuloEtiqueta> listaArticuloEtiqueta = ServiciosArticuloEtiqueta.getInstance().obtenerTodosLosArticulosEtiquetas();
            int cantidad = lista.size();
            List<String> etiquetas = new ArrayList<>();
            for (Articulo arti : lista) {
                etiquetas.add(convertirEtiquetasAJson(arti));
            }






            /*
            for(Articulo arti: lista){ // Recorrer articulos
                for(ArticuloEtiqueta eTI: listaArticuloEtiqueta){ // Recorrer ArticulosEtiquetas
                    if(eTI.getIdArticulo() == arti.getId()){
                        Etiqueta tutiTiqueta = ServiciosEtiquetas.getInstance().getEtiquetaPorId(eTI.getIdEtiqueta());
                        if(tutiTiqueta != null)
                        {
                            etiquetas.add(tutiTiqueta.toString());
                        }
                    }

                }
            }
*/
            Respuesta respuesta = new Respuesta(lista, cantidad, etiquetas);
            ctx.json(respuesta);


            //  List<Articulo> listaArticulos = ServiciosArticulo.getInstance().obtenerTodosLosArticulos();
            //Respuesta respuesta = new Respuesta(listaArticulos, cantidad, listaArticulos);
            // ctx.json(respuesta);


        });

        //TODO: HACER LA MALDITA VAINA DE BUSCAR POR ETIQUETA
        app.get("/cargarArticulosEtiqueta/{etiqueta}", ctx -> {
            String etiqueta = ctx.pathParam("etiqueta");
            //List<Articulo> lista = Controladora.getInstance().getListaArticuloByEtiqueta(etiqueta);
            // Lo que hay que fucking hacer es conseguir la maldita lista de etiuqetas de los malditos articulos
            // Para meter esa maldita vaina en etiquetas
            // y ser putamente feliz, por par de segundo :)

            // Imprimir la maldita palabra
            System.out.println("La putisima etiqueta: " + etiqueta);
            // Imprimir la maldita etiqueta
            Etiqueta tutiTiqueta = ServiciosEtiquetas.getInstance().etiquetaExiste(etiqueta);
            System.out.println("La putisima etiqueta pero ya aqui: " + tutiTiqueta.getEtiqueta());

            // tod0 lo maldito articulo etiqueta
            List<ArticuloEtiqueta> todoTuti = ServiciosArticuloEtiqueta.getInstance().obtenerTodosLosArticulosEtiquetas();

            // tod0 lo maldito articulo
            List<Articulo> articulotodo = ServiciosArticulo.getInstance().obtenerTodosLosArticulos();


            List<String> etiquetas = new ArrayList<>();
            List<Articulo> articuloClean = new ArrayList<>();

            for(Articulo arti: articulotodo){
                for(ArticuloEtiqueta artitUTI: todoTuti){
                    if(artitUTI.getIdEtiqueta() == tutiTiqueta.getId() && arti.getId() == artitUTI.getIdArticulo())
                    {

                        articuloClean.add(arti);
                       // etiquetas.add(convertirEtiquetasAJson(arti));

                    }
                }
            }


            List<Articulo> lista = ServiciosArticulo.getInstance().obtenerTodosLosArticulos();
            List<ArticuloEtiqueta> listaArticuloEtiqueta = ServiciosArticuloEtiqueta.getInstance().obtenerTodosLosArticulosEtiquetas();
            for (Articulo arti : articuloClean) {
                etiquetas.add(convertirEtiquetasAJson(arti));
            }


            int cantidad = articuloClean.size();
            Respuesta respuesta = new Respuesta(articuloClean, cantidad, etiquetas);
            ctx.json(respuesta);

            /*
            int cantidad = lista.size();
            List<String> etiquetas = new ArrayList<>();
            for(Articulo arti: lista){
                etiquetas.add(convertirEtiquetasAJson(arti));
            }

            Respuesta respuesta = new Respuesta(lista, cantidad, etiquetas);
            ctx.json(respuesta);*/
        });

    }


    // FUNCIONES DE UTILIDAD
    private static String convertirAJson(Usuario usuario) {
        Gson gson = new Gson();
        return gson.toJson(usuario);
    }


    static class Respuesta {
        private List<Articulo> articulos;
        private int cantidad;

        private List<String> etiquetas;

        public Respuesta(List<Articulo> articulos, int cantidad, List<String> etiquetas) {
            this.articulos = articulos;
            this.cantidad = cantidad;
            this.etiquetas = etiquetas;
        }

        public List<Articulo> getArticulos() {
            return articulos;
        }

        public int getCantidad() {
            return cantidad;
        }

        public List<String> getEtiquetas() {
            return etiquetas;
        }
    }

    private static String convertirEtiquetasAJson(Articulo articulo) {
        StringBuilder etiquetas = new StringBuilder();

        List<ArticuloEtiqueta> articulosEtiquetas = ServiciosArticuloEtiqueta.getInstance().obtenerTodosLosArticulosEtiquetas();
        for (ArticuloEtiqueta artieti : articulosEtiquetas) {
            if (artieti.getIdArticulo() == articulo.getId()) {
                Etiqueta tutiTiqueta = ServiciosEtiquetas.getInstance().getEtiquetaPorId(artieti.getIdEtiqueta());
                if (tutiTiqueta != null) {
                    etiquetas.append(tutiTiqueta.getEtiqueta()).append(", ");
                }
            }



        }

        if (!etiquetas.isEmpty()) {
            etiquetas.delete(etiquetas.length() - 2, etiquetas.length());
        }

        return etiquetas.toString();
    }


}