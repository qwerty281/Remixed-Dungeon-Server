package javaclient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;

public class JAVAClient
{
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
    {
        String keyStoreLocation = "./keystore.jks";
        String keyStorePassword = "12345678";
        System.setProperty("javax.net.ssl.trustStore", keyStoreLocation); //установка пути к хранилищу сертификатов
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword); //установка пароля от хранилища
        
        ServerConnection serverConnection = new ServerConnection();
        
        while (!serverConnection.is_disabled)
        {
            Scanner cinput = new Scanner(System.in);
            String send = cinput.nextLine();
            
            serverConnection.send(send);
        }
    }
}