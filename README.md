# Purpose

Sample demonstration of using centralized fine grained authorization along with IAM in a distributed solution

# Use case

We will demonstrate basic Banking account operations

* Create account
* See account information

conceptually following diagram illustrates roles and actions along with IAM mapping <br>

![](diagrams/auth-concepts.drawio.svg)

# Data flows

## Provisioning
Following diagram illustrates the provisioning of the authorization services with other part of the solution <br>

![](diagrams/provision-flow.drawio.svg)

## Auth and Authorization Flow
This is end 2 end flow when actor fires a request towards the system and how the request firstly authenticated in gateway then authorized from the service <br>

![](diagrams/auth-flow.drawio.svg)

# Components

1. **account-service**: this is the microservice responsible of handling banking account business capabilities. It is written in `Golang`
2. **keycloak-relation-adapter**: this is the adapter to do the mapping in-between `Keycloak` and `SpiceDB`. It is written in `Jdk 20` and `Spring boot 3.1`
3. **SpiceDB**: Centralized authorization service inspired by `Google Zanzibar`. [SpiceDB]([https://](https://github.com/authzed/spicedb))
4. **Keycloak**: Open source IAM application [Keycloak]([https://github.com/keycloak/keycloak])
5. **Kafka**: Message broker to relay the change events from Keycloak to the adapter
6. **Nginx**: Nginx is used to handle the ingress traffic and integrating with keycloak to do the autentication. Then it will forward the user profile downstream


# HOWTO

## Onboard schema to spicedb
```bash
zed context set first-dev-context :50051 "demo-key" --insecure
zed schema write spicedb-schema.zed --insecure
```

<TODO: explain how to test use cases with keycloak screenshots>

# TODO

- [ ] Nginx integration
- [ ] K8s deployment with sidecar pattern

## Start services

1. Keyloak, kafka, spicedb
2. Nginx

```bash
cd deploy
docker-compose -f docker-compose-keycloak.yaml up -d
docker-compose -f docker-compose-nginx.yaml up -d
```


## Testing
### Prerequisites
> first setup keycloak
TODO: put screenshots for keycloak

### Execute tests
1. Get token for nginx client
```bash
curl -L -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=nginx-cli' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_secret=08bAGFwyaDKyI3ti6QYvhpwXxjUV2awK' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'username=test-user' \
  --data-urlencode 'password=123456' \
  | jq
```

**Verify Token (Optional)**

```bash
curl -L -X POST 'http://localhost:8080/realms/master/protocol/openid-connect/token/introspect' \
   -H "Authorization: Basic bmdpbngtY2xpOkp2d0VWVzB4TGE2eHh3YVhuTXZYaVNPZUV6NXpFa1N5" \
   -H "Accept: application/json" \
   -H "Content-Type: application/x-www-form-urlencoded" \
   --data-urlencode 'token=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJZSmZqalJ1aVNqVi1LWlFuOGVzNzRCTzVWeEQ5NTF6MjNDS2luZDV5eW00In0.eyJleHAiOjE2OTM0MjA5OTEsImlhdCI6MTY5MzQyMDkzMSwianRpIjoiNmEyY2RkMDItNzdlZi00ZDhlLTgyZGEtNmU4YjczNDc0NzRjIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYWM5NmVmMGItMDA3Ni00NjcwLThkOGItNWJjNjNmOTJkZjhmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibmdpbngtY2xpIiwic2Vzc2lvbl9zdGF0ZSI6IjZiNThiMmM2LTg3MGYtNGFlZi05ZTVhLTkxYjdhNzAwNDhlOCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUiLCJzaWQiOiI2YjU4YjJjNi04NzBmLTRhZWYtOWU1YS05MWI3YTcwMDQ4ZTgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6InRlc3QtdXNlciIsImdpdmVuX25hbWUiOiIiLCJmYW1pbHlfbmFtZSI6IiIsImdyb3VwIjpbIi9icmFuY2gtMSJdfQ.qX7NJV7pLTH_fM8WPnRuVlCw7XjvEn-m7FrVrseJZFewn1clPJZ7OOTYSkHqy4hJpXM_RC0s0z8coHaQAKM7og3D5bahDhNp9odK2jsd-75Q4RA9YgyzC-oDcpwPqlVpN1KGP7DTdZ-c_Mjm74vuqEmjrd4Fojbrc_Jn7b9SBdMyf5QBENyNndsWGJTbJqfHFHO4FmcL6ErH-7YHHL7pCq3Qv3BQGoIfEy3qJLE7PTW2SeAX8jB8yZN1fb7MW6uGgve0VdcH77-N6wwrOB_BQI5_Cy69UBxWUvCXn-1KqtgRRiWXc4Ht1jGe9jL1l3V-XmJxZ_0JBnndFWis9KhgBQ' \
   | jq
```

2. Call endpoint with token

```bash
curl -X GET "http://localhost:8085/api/get" \
-H  "accept: application/json" \
-H "Authorization: Bearer <TOKEN>"
```


