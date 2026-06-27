# GraphQL + REST Microservices + WebSocket + gRPC Chat Demo

Proyecto demo con arquitectura por dominios:

```txt
Angular moderno
  ├─ GraphQL HTTP + JWT -> graphql-gateway-service
  │                         ├─ REST interno -> usuario-service
  │                         └─ REST interno -> relaciones-service
  └─ WebSocket + JWT ------> chat-websocket-gateway
                            └─ gRPC bidirectional streaming -> chat-grpc-service
```

## Servicios

| Servicio | Puerto | Tecnología | Función |
|---|---:|---|---|
| usuario-service | 8081 | REST + H2 + Swagger | Gestión de usuarios |
| relaciones-service | 8082 | REST + H2 + Swagger | Amigos y seguidores |
| graphql-gateway-service | 8080 | GraphQL + Resolvers + JWT | API Gateway GraphQL |
| chat-grpc-service | 9090/8090 | gRPC + Protocol Buffers + Netty + H2 | Núcleo del chat |
| chat-websocket-gateway | 8086 | WebSocket + cliente gRPC | Puente Angular → gRPC |
| cliente-angular | 4200 | Angular + Nginx | Frontend |

## Levantar todo

```bash
docker compose up --build
```

Credenciales:

```txt
usuario1 / 1234
usuario2 / 1234
...
usuario20 / 1234
```

## URLs

```txt
Angular:                http://localhost:4200
GraphQL endpoint:       http://localhost:8080/graphql
GraphiQL:               http://localhost:8080/graphiql?path=/graphql
Swagger usuario:        http://localhost:8081/swagger-ui.html
Swagger relaciones:     http://localhost:8082/swagger-ui.html
Swagger chat gateway:   http://localhost:8086/swagger-ui.html
H2 chat:                http://localhost:8090/h2-console
```

## Probar GraphQL con curl

Login:

```bash
curl -X POST http://localhost:8080/graphql   -H "Content-Type: application/json"   -d '{"query":"query { login(username: "usuario1", password: "1234") { authenticated token message user { id username firstName email avatarUrl } } }"}'
```

Panel con token:

```bash
TOKEN="PEGA_AQUI_EL_TOKEN"
curl -X POST http://localhost:8080/graphql   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -d '{"query":"query { me(userId: 1) { id username firstName lastName email phone profession description avatarUrl } userRelations(userId: 1) { type status user { id username firstName lastName email avatarUrl } } }"}'
```

## Probar REST internos

Usuario batch:

```bash
curl -X POST http://localhost:8081/api/users/batch   -H "Content-Type: application/json"   -H "X-Internal-Api-Key: secret-internal-key"   -d '{"ids":[2,3,4,5,6]}'
```

Relaciones:

```bash
curl -X GET http://localhost:8082/api/relationships/user/1   -H "X-Internal-Api-Key: secret-internal-key"
```

## Probar WebSocket del chat

Primero obtén un JWT con el login GraphQL. Luego usa una herramienta como Postman WebSocket, Insomnia, websocat o wscat.

Con `wscat`:

```bash
npm install -g wscat
TOKEN="PEGA_AQUI_EL_TOKEN"
wscat -c "ws://localhost:8086/ws/chat?userId=1&token=$TOKEN"
```

Dentro del socket:

```json
{"type":"HISTORY","fromUserId":1,"toUserId":2}
{"type":"PRESENCE","fromUserId":1,"toUserId":2}
{"type":"MESSAGE","fromUserId":1,"toUserId":2,"content":"Hola desde usuario1"}
```

Abre otra terminal con token de `usuario2`:

```bash
wscat -c "ws://localhost:8086/ws/chat?userId=2&token=$TOKEN_USUARIO_2"
```

## Probar gRPC directo

Instala `grpcurl` y ejecuta:

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 chat.ChatService/GetPresence -d '{"user_id":1}'
```

Historial:

```bash
grpcurl -plaintext -import-path chat-grpc-service/src/main/proto -proto chat.proto localhost:9090 chat.ChatService/GetHistory -d '{"user_id":1,"other_user_id":2}'
```

## Colecciones y pruebas

En la carpeta `testing/` hay:

```txt
graphql-rest.postman_collection.json
websocket-examples.md
grpcurl-commands.md
chat.proto
```

## Concepto clave

- GraphQL no es REST: usa HTTP, pero tiene un único endpoint `/graphql` y resuelve queries con resolvers.
- WebSocket conecta Angular en tiempo real.
- gRPC bidirectional streaming se usa internamente entre `chat-websocket-gateway` y `chat-grpc-service`.


## Fix aplicado para gRPC

El módulo `chat-grpc-service` usa `grpc-server-spring-boot-starter 3.1.0.RELEASE`. Para evitar conflictos de clases entre librerías gRPC, los módulos de chat usan `grpc.version=1.58.0`, alineado con la versión compatible indicada por el starter. Si `chat-grpc-service` no inicia, `chat-websocket-gateway` mostrará `Unable to resolve host chat-grpc-service` porque el contenedor gRPC se cae antes de quedar disponible en la red Docker.

Para levantar limpio:

```bash
docker compose down -v
docker compose build --no-cache chat-grpc-service chat-websocket-gateway cliente-angular
docker compose up
```

Para revisar:

```bash
docker compose ps
docker logs -f chat-grpc-service
docker logs -f chat-websocket-gateway
```
