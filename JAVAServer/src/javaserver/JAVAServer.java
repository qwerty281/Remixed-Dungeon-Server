package javaserver;

import java.io.IOException;

public class JAVAServer
{   
    public static void main(String[] args) throws IOException
    {
        String keyStoreLocation = "./keystore.jks"; //расположение keyStore
        String keyStorePassword = "12345678"; //пароль от keyStore
        String serverPassword = "";
        boolean print_errors = false;
        
        System.setProperty("javax.net.ssl.keyStore", keyStoreLocation); //установка пути к хранилищу сертификатов
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword); //установка пароля от хранилища
        
        for(int i = 0; i < args.length; i++)
        {
            if(args[i].equals("--password"))
            {
                if(args.length > i + 1)
                {
                    serverPassword = args[i + 1];
                }
            }
            
            if(args[i].equals("--print-errors"))
            {
                print_errors = true;
            }
        }
        
        Server server = new Server(serverPassword, print_errors);
    }
}