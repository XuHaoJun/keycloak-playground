package com.github.xuhaojun.keycloak.email.provider;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Rabbitmq091EmailSenderProviderFactory implements EmailSenderProviderFactory {
    private static final Logger log = Logger.getLogger(Rabbitmq091EmailSenderProviderFactory.class);
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        checkConnectionAndChannel();
        return new Rabbitmq091EmailSenderProvider(session, this.channel);
    }

    private synchronized void checkConnectionAndChannel() {
        try {
            if (connection == null || !connection.isOpen()) {
                this.connection = connectionFactory.newConnection();
            }
            if (channel == null || !channel.isOpen()) {
                channel = connection.createChannel();
            }
        } catch (IOException | TimeoutException e) {
            log.error("keycloak-to-rabbitmq ERROR on connection to rabbitmq", e);
        }
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq-svc"); // Set your broker host
            factory.setPort(5672); // Set your broker port
            factory.setVirtualHost("/");
            factory.setUsername("admin");
            factory.setPassword("admin");
            this.connectionFactory = factory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AMQP connection", e);
        }
    }

    @Override
    public void postInit(org.keycloak.models.KeycloakSessionFactory factory) {
        // Post-initialization logic if needed
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                channel.close();
                connection.close();
            }
        } catch (Exception e) {
            // Handle exception
        }
    }

    @Override
    public String getId() {
        return "rabbitmq091-email-sender";
    }
}