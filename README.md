# Mailtools Next

## Pre requisites

- Running postgres database. Check parameters below.

## Firewall

| Target           | IP Port    | 
|------------------|------------|
| europe.bmw.corp  | `tcp/3269` |

## Parameters

### Database

| Name        | Type   | Default value |
|-------------|--------|---------------|
| DB_HOST     | string |               |
| DB_SCHEMA   | string | `mtnext`      |
| DB_USER     | string | `mtnext`      |
| DB_PASSWORD | string |               |
| DB_PORT     | number | `5432`        |
| DB_TIMEOUT  | string | `1200s`       |

### Quarkus

| Name                       | Example   | Default value |
|----------------------------|-----------|---------------|
| RESTEASY_PATH              | string    | `/api`        |
| LOG_LEVEL                  | string    | `INFO`        |
| LOG_CATEGORY_COM_BMW_LEVEL | string    | `FINE`        |
| SWAGGER_ENABLED            | boolean   | `false`       ||

### Health

| Name            | Example   | Default value                      |
|-----------------|-----------|------------------------------------|
| SAFE_PROPERTIES | string    | `password,secret,credential,token` |

### WebEAM Next

| Name                            | Example  | Default value          |
|---------------------------------|----------|------------------------|
| WEBEAM_OIDC_PROVIDER            | string   |                        |
| WEBEAM_OIDC_CLIENT_ID           | string   |                        |
| WEBEAM_OIDC_CLIENT_SECRET       | string   |                        |
| WEBEAM_OIDC_CLIENTSECRET_METHOD | string   | `basic`                |
| WEBEAM_OIDC_SCOPES              | string   | `email,profile,groups` |
| WEBEAM_OIDC_ROLES_SOURCE        | string   | `userinfo`             |
