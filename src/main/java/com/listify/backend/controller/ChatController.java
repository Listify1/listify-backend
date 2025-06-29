package com.listify.backend.controller;

import com.listify.backend.dto.ChatMessageDTO;
import com.listify.backend.model.ChatMessage;
import com.listify.backend.model.Group;
import com.listify.backend.model.User;
import com.listify.backend.model.enums.MessageType;
import com.listify.backend.repository.ChatMessageRepository;
import com.listify.backend.repository.GroupRepository;
import com.listify.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        Optional<User> senderOpt = userRepository.findById(dto.getSenderId());
        Optional<Group> groupOpt = groupRepository.findById(dto.getGroupId());

        if (senderOpt.isEmpty() || groupOpt.isEmpty()) return;

        User sender = senderOpt.get();
        Group group = groupOpt.get();

        if (!group.getUsers().contains(sender)) return;

        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent());
        message.setSender(sender);
        message.setGroup(group);
        message.setTimestamp(LocalDateTime.now());

        // Hier wird der Typ gesetzt (wird auch für 'POLL' funktionieren)
        try {
            message.setType(MessageType.valueOf(dto.getType()));
        } catch (Exception e) {
            message.setType(MessageType.TEXT);
        }

        // Wenn es eine Umfrage ist, speichere die Metadaten
        if (dto.getMetadata() != null && message.getType() == MessageType.POLL) {
            message.setMetadata(dto.getMetadata());
        }

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Das DTO für den Broadcast vorbereiten
        dto.setId(savedMessage.getId()); // Wichtig: ID zurücksenden
        dto.setSenderName(sender.getUsername());
        dto.setTimestamp(savedMessage.getTimestamp().toString());
        dto.setMetadata(savedMessage.getMetadata()); // Auch Metadaten zurücksenden

        messagingTemplate.convertAndSend("/topic/group/" + group.getId(), dto);
    }


    @MessageMapping("/chat.vote")
    public void castVote(@Payload Map<String, Object> payload) {
        // Sicherer Zugriff auf die Payload-Werte
        Long messageId = payload.get("messageId") instanceof Number ? ((Number) payload.get("messageId")).longValue() : null;
        Long optionIndex = payload.get("optionIndex") instanceof Number ? ((Number) payload.get("optionIndex")).longValue() : null;
        Long userId = payload.get("userId") instanceof Number ? ((Number) payload.get("userId")).longValue() : null;

        if (messageId == null || optionIndex == null || userId == null) return;

        Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
        if (messageOpt.isEmpty() || messageOpt.get().getType() != MessageType.POLL) return;

        ChatMessage message = messageOpt.get();
        Map<String, Object> metadata = message.getMetadata();
        if (metadata == null) return;

        // Sicherer Zugriff auf die Optionen im Metadaten-Feld
        List<Map<String, Object>> options = (List<Map<String, Object>>) metadata.get("options");
        if (options == null || optionIndex >= options.size()) return;

        // Logik, um sicherzustellen, dass ein Benutzer nur einmal pro Umfrage abstimmt
        for (Map<String, Object> opt : options) {
            List<Number> voters = (List<Number>) opt.computeIfAbsent("voters", k -> new ArrayList<>());
            voters.removeIf(voterId -> voterId.longValue() == userId);
        }

        Map<String, Object> selectedOption = options.get(optionIndex.intValue());
        List<Number> voters = (List<Number>) selectedOption.computeIfAbsent("voters", k -> new ArrayList<>());

        // Füge die Stimme hinzu (doppelte Stimmen sind bereits durch die obere Schleife entfernt)
        voters.add(userId);

        message.setMetadata(metadata);
        ChatMessage updatedMessage = chatMessageRepository.save(message);

        // Erstelle ein DTO aus der aktualisierten Nachricht, um es an die Clients zu senden
        ChatMessageDTO updatedDto = new ChatMessageDTO(
                updatedMessage.getContent(),
                updatedMessage.getSenderId(),
                updatedMessage.getGroupId(),
                updatedMessage.getSenderName(),
                updatedMessage.getTimestamp().toString(),
                updatedMessage.getType().name(),
                updatedMessage.getMetadata(),
                updatedMessage.getId()
        );

        messagingTemplate.convertAndSend("/topic/group/" + message.getGroup().getId(), updatedDto);
    }

    @GetMapping("/group/{groupId}")
    public List<ChatMessageDTO> getMessagesForGroup(@PathVariable Long groupId) {
        return chatMessageRepository.findByGroup_Id(groupId).stream().map(msg -> {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setId(msg.getId());
            dto.setContent(msg.getContent());
            dto.setGroupId(groupId);
            dto.setTimestamp(msg.getTimestamp().toString());
            dto.setType(msg.getType() != null ? msg.getType().name() : "TEXT");
            dto.setMetadata(msg.getMetadata()); // Wichtig: Metadaten auch hier hinzufügen

            if (msg.getSender() != null) {
                dto.setSenderId(msg.getSender().getId());
                dto.setSenderName(msg.getSender().getUsername());
            } else {
                dto.setSenderId(null);
                dto.setSenderName("Gelöschter Benutzer");
            }
            return dto;
        }).toList();
    }
}