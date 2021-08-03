package javaserver;

import java.io.IOException;


public class JAVAServer
{    
    public static void main(String[] args) throws IOException
    {
        String keyStoreLocation = "./keystore.jks"; //расположение keyStore
        String keyStorePassword = "12345678"; //пароль от keyStore
        
        System.setProperty("javax.net.ssl.keyStore", keyStoreLocation); //установка пути к хранилищу сертификатов
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword); //установка пароля от хранилища
        
        Server server = new Server();
    }
}