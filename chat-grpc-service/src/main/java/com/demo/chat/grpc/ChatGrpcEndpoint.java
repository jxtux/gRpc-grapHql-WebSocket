package com.demo.chat.grpc;

import com.demo.chat.entity.ChatMessageEntity;
import com.demo.chat.entity.UserPresenceEntity;
import com.demo.chat.proto.*;
import com.demo.chat.repository.ChatMessageRepository;
import com.demo.chat.repository.UserPresenceRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
public class ChatGrpcEndpoint extends ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(ChatGrpcEndpoint.class);
    private final ChatMessageRepository messageRepository;
    private final UserPresenceRepository presenceRepository;
    private final Map<Long, CopyOnWriteArrayList<StreamObserver<ChatServerEvent>>> observersByUser = new ConcurrentHashMap<>();

    public ChatGrpcEndpoint(ChatMessageRepository messageRepository, UserPresenceRepository presenceRepository) {
        this.messageRepository = messageRepository;
        this.presenceRepository = presenceRepository;
    }

    @Override
    public StreamObserver<ChatClientEvent> chatStream(StreamObserver<ChatServerEvent> responseObserver) {
        AtomicLong currentUserId = new AtomicLong(0L);
        return new StreamObserver<>() {
            @Override
            public void onNext(ChatClientEvent event) {
                try {
                    if (event.getType() == ChatEventType.CONNECT) {
                        currentUserId.set(event.getFromUserId());
                        addObserver(event.getFromUserId(), responseObserver);
                        markPresence(event.getFromUserId(), true);
                        log.info("Usuario {} conectado al stream gRPC", event.getFromUserId());
                        broadcastPresence(event.getFromUserId(), true);
                        responseObserver.onNext(serverEvent(event.getFromUserId(), 0, "Conectado", ChatEventType.USER_ONLINE, true));
                    } else if (event.getType() == ChatEventType.DISCONNECT) {
                        disconnect(currentUserId.get(), responseObserver);
                    } else if (event.getType() == ChatEventType.MESSAGE) {
                        handleMessage(event, responseObserver);
                    }
                } catch (Exception ex) {
                    log.error("Error procesando evento gRPC", ex);
                    responseObserver.onNext(serverEvent(0, event.getFromUserId(), ex.getMessage(), ChatEventType.ERROR, false));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.warn("Stream gRPC cerrado con error para userId={}: {}", currentUserId.get(), throwable.getMessage());
                disconnect(currentUserId.get(), responseObserver);
            }

            @Override
            public void onCompleted() {
                disconnect(currentUserId.get(), responseObserver);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getHistory(ChatHistoryRequest request, StreamObserver<ChatHistoryResponse> responseObserver) {
        log.info("Consultando historial entre {} y {}", request.getUserId(), request.getOtherUserId());
        List<ChatMessageEntity> messages = messageRepository.findConversation(request.getUserId(), request.getOtherUserId());
        ChatHistoryResponse.Builder builder = ChatHistoryResponse.newBuilder();
        messages.forEach(m -> builder.addMessages(ChatServerEvent.newBuilder()
                .setEventId(String.valueOf(m.getId()))
                .setFromUserId(m.getFromUserId())
                .setToUserId(m.getToUserId())
                .setContent(m.getContent())
                .setType(ChatEventType.MESSAGE_RECEIVED)
                .setCreatedAt(m.getSentAt().toString())
                .setOnline(isOnline(m.getFromUserId()))
                .build()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPresence(UserPresenceRequest request, StreamObserver<UserPresenceResponse> responseObserver) {
        UserPresenceEntity presence = presenceRepository.findById(request.getUserId()).orElse(null);
        responseObserver.onNext(UserPresenceResponse.newBuilder()
                .setUserId(request.getUserId())
                .setOnline(presence != null && presence.isOnline())
                .setLastSeenAt(presence != null && presence.getLastSeenAt() != null ? presence.getLastSeenAt().toString() : "")
                .build());
        responseObserver.onCompleted();
    }

    private void handleMessage(ChatClientEvent event, StreamObserver<ChatServerEvent> senderObserver) {
        Instant now = Instant.now();
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setFromUserId(event.getFromUserId());
        entity.setToUserId(event.getToUserId());
        entity.setContent(event.getContent());
        entity.setSentAt(now);
        entity.setStatus(isOnline(event.getToUserId()) ? "DELIVERED" : "SENT");
        entity = messageRepository.save(entity);
        log.info("Mensaje {} guardado: {} -> {}", entity.getId(), event.getFromUserId(), event.getToUserId());

        ChatServerEvent message = ChatServerEvent.newBuilder()
                .setEventId(String.valueOf(entity.getId()))
                .setFromUserId(entity.getFromUserId())
                .setToUserId(entity.getToUserId())
                .setContent(entity.getContent())
                .setType(ChatEventType.MESSAGE_RECEIVED)
                .setCreatedAt(entity.getSentAt().toString())
                .setOnline(isOnline(entity.getToUserId()))
                .build();

        sendToUser(event.getFromUserId(), message);
        if (event.getFromUserId() != event.getToUserId()) {
            sendToUser(event.getToUserId(), message);
        }
    }

    private void addObserver(Long userId, StreamObserver<ChatServerEvent> observer) {
        observersByUser.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(observer);
    }

    private void disconnect(Long userId, StreamObserver<ChatServerEvent> observer) {
        if (userId == null || userId == 0) return;
        CopyOnWriteArrayList<StreamObserver<ChatServerEvent>> observers = observersByUser.get(userId);
        if (observers != null) {
            observers.remove(observer);
            if (observers.isEmpty()) {
                observersByUser.remove(userId);
                markPresence(userId, false);
                broadcastPresence(userId, false);
                log.info("Usuario {} desconectado del stream gRPC", userId);
            }
        }
    }

    private void sendToUser(Long userId, ChatServerEvent event) {
        observersByUser.getOrDefault(userId, new CopyOnWriteArrayList<>()).forEach(observer -> {
            try { observer.onNext(event); } catch (Exception ex) { log.warn("No se pudo enviar evento a {}", userId); }
        });
    }

    private void broadcastPresence(Long userId, boolean online) {
        ChatServerEvent event = serverEvent(userId, 0, online ? "ONLINE" : "OFFLINE", online ? ChatEventType.USER_ONLINE : ChatEventType.USER_OFFLINE, online);
        observersByUser.values().forEach(list -> list.forEach(observer -> observer.onNext(event)));
    }

    private void markPresence(Long userId, boolean online) {
        UserPresenceEntity p = presenceRepository.findById(userId).orElseGet(UserPresenceEntity::new);
        p.setUserId(userId);
        p.setOnline(online);
        p.setUpdatedAt(Instant.now());
        if (!online) p.setLastSeenAt(Instant.now());
        presenceRepository.save(p);
    }

    private boolean isOnline(Long userId) {
        return presenceRepository.findById(userId).map(UserPresenceEntity::isOnline).orElse(false);
    }

    private ChatServerEvent serverEvent(long from, long to, String content, ChatEventType type, boolean online) {
        return ChatServerEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setFromUserId(from)
                .setToUserId(to)
                .setContent(content)
                .setType(type)
                .setCreatedAt(Instant.now().toString())
                .setOnline(online)
                .build();
    }
}
