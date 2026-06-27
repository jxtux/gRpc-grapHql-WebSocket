# relaciones-service

Microservicio REST del dominio de relaciones: amistades y seguidores.

## Swagger

```txt
http://localhost:8082/swagger-ui.html
```

## Seguridad interna

Los endpoints `/api/**` requieren:

```txt
X-Internal-Api-Key: secret-internal-key
```
