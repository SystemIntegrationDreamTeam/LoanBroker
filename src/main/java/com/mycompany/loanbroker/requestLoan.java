/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.loanbroker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;

/**
 *
 * @author Buhrkall
 */
@WebService(serviceName = "requestLoan")
@Stateless()
public class requestLoan {
    
    
    public final String SENDING_QUEUE_NAME = "CreditScoreQueue";
    public String message = "";
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "request")
    public String request(@WebParam(name = "ssn") String ssn, 
                          @WebParam(name = "loanAmount") double loanAmount, 
                          @WebParam(name = "loanDuration") int loanDuration) throws IOException {
        
        ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("datdb.cphbusiness.dk");
            factory.setVirtualHost("Dreamteam");
            factory.setUsername("DreamTeam");
            factory.setPassword("bastian");
            
            message = ssn + "," + loanAmount + "," + loanDuration;
            
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
    
            channel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", SENDING_QUEUE_NAME, null, message.getBytes());
    
            channel.close();
            connection.close();
        
            System.out.println(ssn);
            System.out.println(loanAmount);
            System.out.println(loanDuration);
            
        return "Hello";
    }

    /**
     * This is a sample web service operation
     */
    
    
    
    
}
