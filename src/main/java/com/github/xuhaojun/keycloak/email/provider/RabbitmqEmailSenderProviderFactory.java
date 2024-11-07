package com.github.xuhaojun.keycloak.email.provider;

import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitmqEmailSenderProviderFactory implements EmailSenderProviderFactory {

    private Connection amqpConnection;

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        return new RabbitmqEmailSenderProvider(session, amqpConnection);
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost"); // Set your broker host
            factory.setPort(5672); // Set your broker port
            // Set other connection properties as needed
            amqpConnection = factory.newConnection();
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
            if (amqpConnection != null) {
                amqpConnection.close();
            }
        } catch (Exception e) {
            // Handle exception
        }
    }

    @Override
    public String getId() {
        return "custom-email-sender";
    }
} 