# graphql-gateway-service

Servicio Spring Boot que actúa como API Gateway GraphQL.

## Endpoints

- GraphQL: http://localhost:8080/graphql
- GraphiQL: http://localhost:8080/graphiql
- Swagger auxiliar: http://localhost:8080/swagger-ui.html

## Resolvers

Los resolvers están en `graphql/GraphQLResolver.java`.

- `login(username, password)` llama al `usuario-service` y genera JWT.
- `me(userId)` valida JWT y llama al `usuario-service`.
- `userRelations(userId)` valida JWT, llama al `relaciones-service`, luego llama al `usuario-service` con batch.
