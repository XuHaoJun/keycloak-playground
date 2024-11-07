# Custom Email Sender Provider

This project implements a custom `EmailSenderProvider` and `EmailSenderProviderFactory` for Keycloak. It publishes email messages to the "amp.topic" exchange with the routing key "KK.EMAIL.SEND".

## Setup

1. Clone the repository.
2. Add necessary dependencies for Keycloak and your messaging library (e.g., RabbitMQ client).
3. Implement the logic to connect to your message broker and publish messages.
4. Deploy the provider to your Keycloak instance.

## Usage

- Configure your Keycloak realm to use the custom email sender provider.
- Ensure your message broker is running and accessible.
