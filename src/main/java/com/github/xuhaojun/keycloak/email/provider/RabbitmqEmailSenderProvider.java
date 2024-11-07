package com.github.xuhaojun.keycloak.email.provider;

import java.util.Map;

import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RabbitmqEmailSenderProvider implements EmailSenderProvider {

    private final KeycloakSession session;
    private final Connection amqpConnection;

    public RabbitmqEmailSenderProvider(KeycloakSession session, Connection amqpConnection) {
        this.session = session;
        this.amqpConnection = amqpConnection;
    }

    @Override
    public void send(Map<String, String> map, String subject, String textBody, String htmlBody, String mimeType) {
        try (Channel channel = amqpConnection.createChannel()) {
            String message = "Subject: " + subject + "\nText: " + textBody + "\nHTML: " + htmlBody;
            channel.basicPublish("amp.topic", "KK.EMAIL.SEND", null, message.getBytes());
        } catch (Exception e) {
            // Handle exception
        }
    }

    @Override
    public void close() {
        // Clean up resources if needed
    }
} 