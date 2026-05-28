# Deployment

0. `docker login ghcr.io --username <username> --password <password>`

## - Keycloak

1. `docker build --platform linux/amd64 -f environments/keycloak/Dockerfile -t ghcr.io/<username>/ledgerview-keycloak:<version> .`
2. `docker push ghcr.io/<username>/ledgerview-keycloak:<version>`

## - Spring backend

1. `./mvnw clean package -DskipTests` (might require `chmod +x mvnw && ./mvnw clean package -DskipTests`)
2. `docker build --platform linux/amd64 -f Dockerfile -t ghcr.io/<username>/ledgerview-backend:<version> .`
3. `docker push ghcr.io/<username>/ledgerview-backend:<version>`
