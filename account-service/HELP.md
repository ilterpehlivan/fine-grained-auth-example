# Create account
```bash
echo -n '{"owner_email": "test@example","currency":"CAD"}' | http POST localhost:8080/v1/accounts X_USER_ID:123 X_USER_GROUPS:/branch-1/group-2:${Keycloak_group_2_id}
```

#Read account

## Success cases

1. Read as agent
```bash
http GET localhost:8080/v1/accounts/1 X_USER_ID:123
```

2. Read as owner
```bash
http GET localhost:8080/v1/accounts/1 X_USER_ID:123
```

