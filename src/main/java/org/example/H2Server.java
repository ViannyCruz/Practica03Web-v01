package org.example;

import org.h2.tools.Server;

import java.sql.SQLException;

public class H2Server {

    private static H2Server instancia;

    private H2Server(){

    }

    public static H2Server getInstancia(){
        if(instancia == null){
            instancia=new H2Server();
        }
        return instancia;
    }

    public void startDb() {
        try {
            //Modo servidor H2.
            Server.createTcpServer("-tcpPort",
                    "9092",
                    "-tcpAllowOthers",
                    "-tcpDaemon",
                    "-ifNotExists").start();
            //Abriendo el cliente web. El valor 0 representa puerto aleatorio.
            String status = Server.createWebServer("-trace", "-webPort", "0").start().getStatus();
            //
            System.out.println("Status Web: "+status);
        }catch (SQLException ex){
            System.out.println("Problema con la base de datos: "+ex.getMessage());

        }
    }

    public void init(){
        startDb();
    }
}
