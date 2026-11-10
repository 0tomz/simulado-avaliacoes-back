# simulado-avaliacoes-back

API REST de avaliações em uma empresa: um usuário avalia outras pessoas. Todas as rotas exigem token do Auth0.

- Java 25, Spring Boot 4.0.2, Maven, PostgreSQL, Spring Security (OAuth2 Resource Server).
- `POST /avaliacoes`: cadastra avaliação (somente ADMIN).
- `GET /avaliacoes`: lista avaliações (ADMIN e USER).
- `DELETE /avaliacoes/{id}`: exclui avaliação (somente ADMIN).
- Claims do Auth0: email em `https://social-insper.com/email` e roles em `https://social-insper.com/roles`.

## Configuração

Em `src/main/resources/application.properties`, troque os placeholders pelos valores da sua conta Auth0:

- `TROCAR-AUTH0-DOMAIN` (domínio, ex.: `meu-tenant.us.auth0.com`)
- `TROCAR-AUTH0-AUDIENCE` (identificador da API)

O banco é configurado por variáveis de ambiente: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (padrões locais no `application.properties`).

## Como rodar local

```bash
./mvnw package          # gera target/avaliacoes-0.0.1-SNAPSHOT.jar
docker compose up --build   # sobe postgres + app em http://localhost:8080
docker compose down -v      # encerra e apaga o banco
```

Sem token, `GET http://localhost:8080/avaliacoes` responde `401`.

## Deploy

Fluxo: **push na `main` → `.github/workflows/deploy.yml` → container na AWS**.

1. O GitHub Actions compila com `mvn -B package` (JDK 25).
2. Faz build da imagem com o `Dockerfile` e push no Docker Hub com a tag `github.sha`.
3. Conecta por SSH na máquina da AWS, para o container antigo (`|| true` no primeiro deploy) e sobe o novo em `8080` na rede `rede`, com as variáveis do banco.

### GitHub Secrets (Settings > Secrets and variables > Actions)

| Secret | O que é |
|---|---|
| `DOCKERHUB_USERNAME` | Usuário do Docker Hub (dono da imagem `avaliacoes-ci`). |
| `DOCKERHUB_TOKEN` | Access token do Docker Hub usado no login do workflow. |
| `HOST_TEST` | IP ou DNS público da máquina na AWS. |
| `KEY_TEST` | Chave privada SSH (conteúdo do `.pem`) para o usuário `ubuntu`. |
| `DB_HOST` | Host do PostgreSQL acessível pelo container. |
| `DB_PORT` | Porta do PostgreSQL (normalmente `5432`). |
| `DB_NAME` | Nome do banco. |
| `DB_USER` | Usuário do banco. |
| `DB_PASSWORD` | Senha do banco. |

Pré-requisitos na AWS: Docker instalado e a rede Docker `rede` criada (`docker network create rede`).
