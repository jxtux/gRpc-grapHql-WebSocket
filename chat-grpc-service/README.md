# chat-grpc-service

Microservicio central de chat con gRPC bidirectional streaming, Protocol Buffers, Netty y H2.

Puertos:

- REST auxiliar / H2: `8090`
- gRPC Netty: `9090`

Contrato gRPC:

```txt
src/main/proto/chat.proto
```

Métodos principales:

- `ChatStream`: streaming bidireccional para eventos CONNECT, DISCONNECT y MESSAGE.
- `GetHistory`: historial entre dos usuarios.
- `GetPresence`: presencia online/offline.

Prueba rápida con grpcurl:

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext localhost:9090 chat.ChatService/GetPresence -d '{"user_id":1}'
```
