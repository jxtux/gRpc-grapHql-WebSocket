# usuario-service

Microservicio REST del dominio de gestión de usuarios.

## Ejecutar local

```bash
mvn spring-boot:run
```

## Swagger

```txt
http://localhost:8081/swagger-ui.html
```

## Seguridad interna

Los endpoints `/api/**` requieren:

```txt
X-Internal-Api-Key: secret-internal-key
```

## Endpoints principales

- `POST /api/users/login`
- `GET /api/users/{id}`
- `POST /api/users/batch`
- `GET /api/users`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
