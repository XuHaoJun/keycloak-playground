package com.github.xuhaojun.keycloak.email.provider;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailSenderProviderFactory;
import org.keycloak.models.KeycloakSession;

import com.rabbitmq.client.amqp.*;
import com.rabbitmq.client.amqp.impl.AmqpEnvironmentBuilder;

public class Rabbitmq10EmailSenderProviderFactory implements EmailSenderProviderFactory {
    private static final Logger log = Logger.getLogger(Rabbitmq10EmailSenderProviderFactory.class);
    private AmqpEnvironmentBuilder enviromentFactory;
    private Environment enviroment;
    private Connection connection;
    private Publisher publisher;

    @Override
    public EmailSenderProvider create(KeycloakSession session) {
        checkConnectionAndChannel();
        return new Rabbitmq10EmailSenderProvider(session, this.publisher);
    }

    private synchronized void checkConnectionAndChannel() {
        try {
            if (connection == null) {
                this.connection = enviroment.connectionBuilder().build();
                this.publisher = connection.publisherBuilder().exchange("amq.topic").key("KK.EMAIL.SEND").build();
            }
        } catch (Exception e) {
            log.error("keycloak-to-rabbitmq ERROR on connection to rabbitmq", e);
        }
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        try {
            AmqpEnvironmentBuilder factory = new AmqpEnvironmentBuilder();
            factory.connectionSettings().host("rabbitmq-svc").port(5672).virtualHost("/").username("admin")
                    .password("admin");
            this.enviromentFactory = factory;
            this.enviroment = enviromentFactory.build();
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
            if (enviroment != null) {
                enviroment.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            // Handle exception
        }
    }

    @Override
    public String getId() {
        return "rabbitmq10-email-sender";
    }
}