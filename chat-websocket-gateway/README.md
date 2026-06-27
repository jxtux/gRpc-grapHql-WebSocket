# chat-websocket-gateway

Gateway WebSocket para el navegador. Angular se conecta por WebSocket y este servicio se comunica internamente con `chat-grpc-service` usando gRPC bidirectional streaming.

Puerto:

```txt
http://localhost:8086
ws://localhost:8086/ws/chat?userId=1&token=JWT
```

Swagger auxiliar:

```txt
http://localhost:8086/swagger-ui.html
```

Eventos JSON desde Angular:

```json
{"type":"HISTORY","fromUserId":1,"toUserId":2}
{"type":"PRESENCE","fromUserId":1,"toUserId":2}
{"type":"MESSAGE","fromUserId":1,"toUserId":2,"content":"Hola"}
```
