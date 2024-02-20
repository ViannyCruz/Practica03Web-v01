package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

          //  if(ServiciosUsuario.getInstance().GetUsuarioAct() != null)
          //  {
                String usuarioJson = convertirAJson(ServiciosUsuario.getInstance().GetUsuario());
                ServiciosUsuario.getInstance().SetUsuario(null);

                // Enviar los datos del usuario al cliente
                ctx.json(usuarioJson);
           // }

            //ctx.status(200);

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
                    ctx.cookie("usernameCookie", usernameEncripted, 200);

                    // Cookie Password
                    String passwordEncripted = textEncryptor.encrypt(user.getPassword());
                    ctx.cookie("passwordCookie", passwordEncripted, 200);
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
            etiqueta = etiqueta + ", ";
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










        // Manejar la solicitud POST para cargar el artículo
        app.post("/cargar-articulo", ctx -> {
            // Obtener el ID del artículo enviado desde el cliente
            String body = ctx.body();
            JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
            long articleId = jsonObject.get("id").getAsLong();
            System.out.println("TUTUUTUTUTU: "+ articleId);



           // Articulo articulo = BlogController.getInstance().getArticuloAct(articleId);
            Articulo articulo = ServiciosArticulo.getInstance().obtenerArticuloPorId(articleId);
            ServiciosArticulo.getInstance().SetArticuloAct(articulo);
          //  BlogController.getInstance().setArticuloAct(articulo);

            ctx.status(200); //solicitud exitosa
        });



/*
        app.get("/obtener-articulo", ctx -> {

            // Convertir el artículo a JSON
            Gson gson = new Gson();
            String articuloJson = gson.toJson(ServiciosArticulo.getInstance().GetArticuloAct());

            // Enviar el artículo al cliente
            ctx.result(articuloJson).contentType("application/json; charset=utf-8");
            ctx.status(200);
        });
*/



        app.get("/obtenerArticulo", ctx -> {

            Articulo articulo = ServiciosArticulo.getInstance().GetArticuloAct();

            //LISTA DE COMENTARIOS
            //TODO: OBTENER LA LSITA REAL DE COMENTARIOS
            // NO ESTA VACIA QUE PUSE PARA PROBAR
            List<Comentario> todoLosComentarios = ServiciosComentario.getInstance().obtenerTodosLosComentarios();

            List<Comentario> comentarios = new ArrayList<>();

            long idArticulo = ServiciosArticulo.getInstance().GetArticuloAct().getId();


            for(Comentario coment : todoLosComentarios){
                if(coment != null)
                    if(coment.getIdArticulo() == idArticulo)
                        comentarios.add(coment);
            }




            int cantidadComentarios = comentarios.size();
            List<String> etiquetas = new ArrayList<>();

            String nombreAutor = ServiciosUsuario.getInstance().buscarUsuarioPorId(ServiciosArticulo.getInstance().GetArticuloAct().getAutor()).getNombre();


            etiquetas.add(convertirEtiquetasAJson(articulo));



            Respuesta2 respuesta = new Respuesta2(articulo, comentarios, etiquetas,nombreAutor,cantidadComentarios);
            ctx.json(respuesta);

        });


        app.get("/obtener-articulo-modify", ctx -> {

            // Convertir el artículo a JSON
            Gson gson = new Gson();
            String articuloJson = gson.toJson( ServiciosArticulo.getInstance().GetArticuloAct());

            // Enviar el artículo al cliente
            ctx.result(articuloJson).contentType("application/json; charset=utf-8");
        });


        app.post("/modificarArticulo", ctx -> {
            System.out.println("Entro a modificar articulo, llamo boton !!!!!!!!!!!!!!!");


            String titulo = ctx.formParam("titulo");
            String cuerpo = ctx.formParam("cuerpo");
            String etiquetas = ctx.formParam("etiquetas");

            //Borrar todas los articulos etiqueta de este articulo
            ServiciosArticuloEtiqueta.getInstance().borrarArticuloEtiquetaPorIdArticulo(ServiciosArticulo.getInstance().GetArticuloAct().getId());


            // Obtener el ID del artículo actual
            long articuloId = ServiciosArticulo.getInstance().GetArticuloAct().getId();

            // Modificar el artículo con el nuevo título y cuerpo
            ServiciosArticulo.getInstance().modificarArticuloPorId(articuloId, titulo, cuerpo);

            // Dividir la cadena de etiquetas en etiquetas individuales
            String[] etiquetaSplit = etiquetas.split(",");

            // Recorrer cada etiqueta
            for (String etiqueta : etiquetaSplit) {
                // Limpiar la etiqueta eliminando espacios al principio y al final
                etiqueta = etiqueta.trim();
                System.out.println(etiqueta + "\n");
                // Verificar si la etiqueta ya existe en la base de datos
                Etiqueta existente = ServiciosEtiquetas.getInstance().etiquetaExiste(etiqueta);
                System.out.println(existente);
                if (existente == null) {
                    // Si la etiqueta no existe, créala y obtén su ID
                    Etiqueta nuevaEtiqueta = ServiciosEtiquetas.getInstance().crearEtiqueta(etiqueta);
                    // Asocia la nueva etiqueta al artículo
                    ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(new ArticuloEtiqueta(articuloId, nuevaEtiqueta.getId()));
                } else {
                    // Si la etiqueta ya existe, obtén su ID y asóciala al artículo
                    ServiciosArticuloEtiqueta.getInstance().almacenarArticuloEtiqueta(new ArticuloEtiqueta(articuloId, existente.getId()));
                }
            }

            ctx.status(200);
        });

        app.post("/eliminarArticulo", ctx -> {
            Articulo articulo = ServiciosArticulo.getInstance().GetArticuloAct();
            ServiciosArticulo.getInstance().eliminarArticuloPorId(articulo.getId());
            ServiciosArticulo.getInstance().SetArticuloAct(null);

            //Fuera buena practica eliminar los comentarios de ese articulo tambien
        });






        app.get("/cargarUsuario", ctx -> {
            Usuario user = ServiciosUsuario.getInstance().GetUsuarioAct();

            ctx.json(user);
        });

        app.get("/obtenerUsuario", ctx -> {
            String usuarioJson = convertirAJson(ServiciosUsuario.getInstance().GetUsuarioAct());
            ctx.json(ServiciosUsuario.getInstance().GetUsuarioAct());
            //ctx.json(usuarioJson);
        });

        app.post("/logout", ctx -> {

            // Eliminar la cookie de nombre "usernameCookie"
               ctx.removeCookie("usernameCookie");
            // Eliminar la cookie de nombre "passwordCookie"
               ctx.removeCookie("passwordCookie");

            String username = ctx.formParam("username");
            System.out.println("Deslogueando al usuario: " +username);
            Usuario nadie = new Usuario(null,null,null,false,false);
            ServiciosUsuario.getInstance().SetUsuarioAct(nadie);

        });


        app.post("/crearComentario", ctx -> {
            String comentarioTexto = ctx.formParam("Comentario");
            Usuario autor = ServiciosUsuario.getInstance().buscarUsuarioPorId(ServiciosArticulo.getInstance().GetArticuloAct().getAutor());
            //Long id = Controladora.getInstance().crearComentarioId();



            //Comentario comentario = new Comentario( comentarioTexto, autor);
            Comentario comentario = ServiciosComentario.getInstance().crearComentario(comentarioTexto, ServiciosUsuario.getInstance().GetUsuarioAct().getId(), ServiciosUsuario.getInstance().GetUsuarioAct().getNombre(), ServiciosArticulo.getInstance().GetArticuloAct().getId());


            //Controladora.getInstance().getArticuloSeleccionado().getListaComentarios().add(comentario);

            ctx.json(comentario);
        });



        //REGISTRAR USUARIO
        app.post("/registrar-usuario", ctx -> {
            // Obtener los parámetros enviados en la solicitud POST
            String nombreUsuario = ctx.formParam("username");
            String nombre = ctx.formParam("nombre");
            String contrasena = ctx.formParam("contrasena");

            // Manejar los parámetros booleanos
            boolean esAdministrador = Boolean.parseBoolean(ctx.formParam("esAdministrador"));
            boolean esAutor = Boolean.parseBoolean(ctx.formParam("esAutor"));

            //NUEVO USUARIO
            //Crear el nuevo usuario
            Usuario newUser = new Usuario(nombreUsuario, nombre, contrasena, esAdministrador, esAutor);

            //Agregar el nuevo usuario
            ServiciosUsuario.getInstance().CrearUsuario(nombreUsuario, nombre, contrasena, esAdministrador, esAutor);
            //BlogController.getInstance().addUsuario(newUser);


        });



        app.post("/guardarFoto", ctx -> {
            System.out.println("Entre a guardar fotot !!!!!!!!!!!!!!!!!!!!!!!");

            //String base64Image = ctx.body();
           // ServiciosFoto.getInstance().guardarFoto(base64Image, ServiciosUsuario.getInstance().GetUsuarioAct().getId());
           // System.out.println("Imagen recibida correctamente en el servidor.");
            //ctx.status(200).result("Imagen recibida correctamente en el servidor.");
        });

        app.get("/obtenerFotoUsuario", ctx -> {
            // Aquí deberías escribir el código para obtener la foto del usuario
            // y devolverla al cliente en formato base64 o en el formato adecuado
            // Supongamos que tienes una función que devuelve la foto del usuario en base64

            String fotoBase64 = ServiciosFoto.getInstance().obtenerFotoUsuarioPorId(ServiciosUsuario.getInstance().GetUsuarioAct().getId()); // Debes implementar esta función

            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                ctx.result(fotoBase64); // Devolver la foto como respuesta al cliente
            } else {
                ctx.status(404); // Indicar que la foto no fue encontrada (código de error HTTP 404)
            }
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





    static class Respuesta2 {
        private Articulo articulo;
        private List<Comentario> comentarios;

        private List<String> etiquetas;

        private String nombreAutor;

        private int cantidadComentarios;

        public Respuesta2(Articulo articulo, List<Comentario> comentarios, List<String> etiquetas, String nombreAutor, int cantidadComentarios) {
            this.articulo = articulo;
            this.comentarios = comentarios;
            this.etiquetas = etiquetas;
            this.nombreAutor = nombreAutor;
            this.cantidadComentarios = cantidadComentarios;
        }

        public Articulo getArticulo() {
            return articulo;
        }

        public List<Comentario> getComentarios() {
            return comentarios;
        }

        public List<String> getEtiquetas(){
            return  etiquetas;
        }

        public String getNombreAutor(){
            return nombreAutor;
        }

        public int getCantidadComentarios(){
            return cantidadComentarios;
        }
    }

}