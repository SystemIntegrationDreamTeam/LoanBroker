/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.loanbroker;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
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
    public final String LISTENING_QUEUE_NAME = "BestQuoteQueue";
    public final String EXCHANGE = "BestQuote";
    public String message = "";
    String result = "No Result found";

    /**
     * Web service operation
     */
    @WebMethod(operationName = "request")
    public String request(@WebParam(name = "ssn") String ssn,
            @WebParam(name = "loanAmount") double loanAmount,
            @WebParam(name = "loanDuration") int loanDuration) throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        Channel sendingchannel = connection.createChannel();
        Channel listeningChannel = connection.createChannel();

        listeningChannel.exchangeDeclare(EXCHANGE, "direct");
        listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
        listeningChannel.queueBind(LISTENING_QUEUE_NAME,EXCHANGE, ssn.replace("-", ""));
        
        sendingchannel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);

        message = ssn + "," + loanAmount + "," + loanDuration;

        sendingchannel.basicPublish("", SENDING_QUEUE_NAME, null, message.getBytes());

        sendingchannel.close();

        Consumer consumer = new DefaultConsumer(listeningChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] The LoanBroker Has Received '" + message + "'");
                result = message;                
            }
        };
        
        listeningChannel.basicConsume(LISTENING_QUEUE_NAME, true, consumer);
        //connection.close();

        return result;        
    }
}
