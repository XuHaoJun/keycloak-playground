package com.github.xuhaojun.keycloak.email.provider;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.amqp.*;

public class Rabbitmq10EmailSenderProvider implements EmailSenderProvider {

    private final KeycloakSession session;
    private Publisher publisher;

    public Rabbitmq10EmailSenderProvider(KeycloakSession session, Publisher publisher) {
        this.session = session;
        this.publisher = publisher;
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
            this.publish(message);
        } catch (Exception e) {
            EmailException emailException = new EmailException(e.getMessage());
            emailException.initCause(e);
            throw emailException;
        }
    }

    @Override
    public void send(Map<String, String> config, UserModel user, String subject, String textBody, String htmlBody)
            throws EmailException {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("config", config);
            messageMap.put("subject", subject);
            messageMap.put("textBody", textBody);
            messageMap.put("htmlBody", htmlBody);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                if (user.getId() != null) {
                    userMap.put("id", user.getId());
                }
                if (user.getUsername() != null) {
                    userMap.put("username", user.getUsername());
                }
                userMap.put("isEmailVerified", user.isEmailVerified());
                if (user.getEmail() != null) {
                    userMap.put("email", user.getEmail());
                }
                if (user.getFirstName() != null) {
                    userMap.put("firstName", user.getFirstName());
                }
                if (user.getLastName() != null) {
                    userMap.put("lastName", user.getLastName());
                }
                Map<String, List<String>> attributes = user.getAttributes();
                if (attributes != null && !attributes.isEmpty()) {
                    userMap.put("attributes", attributes);
                }
                messageMap.put("user", userMap);
            }
            String message = new ObjectMapper().writeValueAsString(messageMap);
            this.publish(message);
        } catch (Exception e) {
            EmailException emailException = new EmailException(e.getMessage());
            emailException.initCause(e);
            throw emailException;
        }
    }

    private void publish(String message) throws EmailException {
        AtomicReference<Publisher.Context> contextHolder = new AtomicReference<>();
        publisher.publish(publisher.message(message.getBytes(StandardCharsets.UTF_8)), context -> {
            if (context.status() != Publisher.Status.ACCEPTED) {
                contextHolder.set(context);
            }
        });
        Publisher.Context storedContext = contextHolder.get();
        if (storedContext != null && storedContext.status() != Publisher.Status.ACCEPTED) {
            throw new EmailException(
                    "rabbitmq send email error, rabbitmq response status" + storedContext.status().toString());
        }
    }

    @Override
    public void close() {
        // Clean up resources if needed
    }
}