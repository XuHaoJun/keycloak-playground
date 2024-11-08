package com.github.xuhaojun.keycloak.email.provider;

import java.util.Map;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

public class RabbitmqEmailSenderProvider implements EmailSenderProvider {

    private final KeycloakSession session;
    private final Channel channel;

    public RabbitmqEmailSenderProvider(KeycloakSession session, Channel channel) {
        this.session = session;
        this.channel = channel;
    }

    @Override
    public void send(Map<String, String> config, String subject, String textBody, String htmlBody, String mimeType)
            throws EmailException {
        try {
            Map<String, Object> messageMap = Map.of(
                    "config", config,
                    "subject", subject,
                    "textBody", textBody,
                    "htmlBody", htmlBody,
                    "mimeType", mimeType);
            String message = new ObjectMapper().writeValueAsString(messageMap);
            channel.basicPublish("amq.topic", "KK.EMAIL.SEND", null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new EmailException(e.toString());
            // Handle exception
        }
    }

    @Override
    public void send(Map<String, String> config, UserModel user, String subject, String textBody, String htmlBody)
            throws EmailException {
        try {
            Map<String, Object> messageMap = Map.of(
                    "config", config,
                    "subject", subject,
                    "textBody", textBody,
                    "htmlBody", htmlBody,
                    "user", user != null ? Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "isEmailVerified", user.isEmailVerified(),
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "attributes", user.getAttributes()) : null);
            String message = new ObjectMapper().writeValueAsString(messageMap);
            channel.basicPublish("amq.topic", "KK.EMAIL.SEND", null, message.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new EmailException(e.toString());
        }
    }

    @Override
    public void close() {
        // Clean up resources if needed
    }
}