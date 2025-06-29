package com.listify.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures WebSocket and STOMP message brokering for the application.
 * <p>
 * The {@code @EnableWebSocketMessageBroker} annotation enables WebSocket message handling,
 * backed by a message broker. This class sets up the necessary endpoints and message prefixes
 * to facilitate real-time, two-way communication between the server and clients.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registers the STOMP endpoints, mapping each to a specific URL and (optionally)
     * enabling SockJS fallback options.
     * <p>
     * This method defines the entry point for WebSocket connections. Clients will connect
     * to the {@code /ws} endpoint to initiate the WebSocket handshake.
     * SockJS is enabled to provide a fallback for browsers that do not support WebSocket.
     *
     * @param registry the registry to configure STOMP endpoints.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allows connections from any origin, should be restricted in production.
                .withSockJS();
    }

    /**
     * Configures the message broker that will be used to route messages from one client to another.
     * <p>
     * This method sets up two main components:
     * <ol>
     *   <li>A simple, in-memory message broker for destinations prefixed with {@code /topic}.
     *       Messages sent to these destinations are broadcast to all subscribed clients.</li>
     *   <li>An application destination prefix {@code /app}. Messages sent by clients to destinations
     *       with this prefix (e.g., {@code /app/chat}) will be routed to {@code @MessageMapping}-annotated
     *       methods in controller classes.</li>
     * </ol>
     *
     * @param registry the registry to configure the message broker.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enables a simple broker that broadcasts messages to clients on destinations prefixed with "/topic".
        registry.enableSimpleBroker("/topic");
        // Designates the "/app" prefix for messages that are bound for @MessageMapping-annotated methods.
        registry.setApplicationDestinationPrefixes("/app");
    }
}