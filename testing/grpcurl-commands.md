# Pruebas gRPC con grpcurl

Listar servicios:

```bash
grpcurl -plaintext -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 list
```

Presencia:

```bash
grpcurl -plaintext -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 chat.ChatService/GetPresence -d '{"user_id":1}'
```

Historial:

```bash
grpcurl -plaintext -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 chat.ChatService/GetHistory -d '{"user_id":1,"other_user_id":2}'
```

Streaming bidireccional manual:

```bash
grpcurl -plaintext -d @ -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 chat.ChatService/ChatStream
```

Luego pega eventos JSON, uno por línea:

```json
{"event_id":"1","from_user_id":1,"type":"CONNECT"}
{"event_id":"2","from_user_id":1,"to_user_id":2,"content":"Hola por gRPC","type":"MESSAGE"}
```
