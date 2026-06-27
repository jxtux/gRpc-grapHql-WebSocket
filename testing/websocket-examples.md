# Pruebas WebSocket

1. Obtén token con GraphQL login.
2. Conecta con `wscat`:

```bash
npm install -g wscat
TOKEN="PEGA_AQUI_EL_TOKEN_USUARIO_1"
wscat -c "ws://localhost:8086/ws/chat?userId=1&token=$TOKEN"
```

Enviar historial:

```json
{"type":"HISTORY","fromUserId":1,"toUserId":2}
```

Consultar presencia:

```json
{"type":"PRESENCE","fromUserId":1,"toUserId":2}
```

Enviar mensaje:

```json
{"type":"MESSAGE","fromUserId":1,"toUserId":2,"content":"Hola usuario2"}
```

Para simular otra máquina, abre otra terminal/navegador con `usuario2` y su token.
