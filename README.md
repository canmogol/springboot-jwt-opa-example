# Spring Boot JWT Example

### Keycloak

You need an OAuth2 backend to generate the JWT token, for this purpose you can use Keycloak. 

Run the Keycloak with the username and password as "admin", then you can log in to Keycloak console `http://localhost:8080/auth/admin/master/console/` using 'admin' user with 'admin' password. 

Import the `demo-realm-export.json` file to create the `demo` realm with `user-service` client, `USER_QUERY_AUTH_ROLES` and `USER_QUERY` roles and mappings for `authorities` and `user_name` fields in JWT which are mandatory for default JWT handling in the Spring Security.

Also, you need to update the `security.oauth2.resource.jwt.key-value` entry in the `application.yml` file with the `demo` realm's `RS256 Public key`. When you'ra at "Demo" realm, click "Realm Settings", click "Keys" tab, and click "Public key" button, copy the content of the popup window to the "application.yml" file.

```
docker run -p 8080:8080 --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin jboss/keycloak:7.0.0
```

After that you need to create a user 'myra.manson' with password 'myra'. Set the 'User Enabled' and 'Email Verified' to 'On'. Update the password under the 'Credentials' tab and set as 'Temporary' to 'Off'. Go to 'Role Mappings', under the 'Client Roles' chose the 'user-service', add the 'USER_QUERY_AUTH_ROLES' and 'USER_QUERY' roles to 'Assigned Roles'.

### Postman

You can import the `JWT-Keycloak.postman_collection.json` file into Postman application and test the REST API. The `GET NEW TOKEN` request uses the `myra.manson` user which you need to create in Keycloak, or you can create any user in the `Demo` realm and use that user.
