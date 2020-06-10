# Spring Boot JWT Example

### Keycloak

You need an OAuth2 backend to generate the JWT token, for this purpose you can use Keycloak. 

Run the Keycloak with the username and password as "admin", then you can log in to Keycloak console `http://localhost:8080/auth/admin/master/console/` using 'admin' user with 'admin' password. 

Import the `demo-realm-export.json` file to create the `demo` realm with `user-service` client, `USER_QUERY_AUTH_ROLES` and `USER_QUERY` roles and mappings for `authorities` and `user_name` fields in JWT which are mandatory for default JWT handling in the Spring Security.

Also, you need to update the `security.oauth2.resource.jwt.key-value` entry in the `application.yml` file with the `demo` realm's `RS256 Public key`. When you'ra at "Demo" realm, click "Realm Settings", click "Keys" tab, and click "Public key" button, copy the content of the popup window to the "application.yml" file.

```
docker run -p 8080:8080 --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin jboss/keycloak:7.0.0
```

After that you need to create a user 'myra.manson' with password 'myra'. Set the 'User Enabled' and 'Email Verified' to 'On'. Update the password under the 'Credentials' tab and set as 'Temporary' to 'Off'. Go to 'Role Mappings', under the 'Client Roles' chose the 'user-service', add the 'USER_QUERY_AUTH_ROLES' role to 'Assigned Roles'.

### OPA (Open Policy Agent)

You need the OPA backend to validate the access control, for this purpose you need to use OPA, and run the OPA with the example REGO file `api_authz.rego` which is located under the `main/resources/` folder. The endpoint for the REST APIs is defined in the `application.yaml` as `http://localhost:8181/v1/data/restapi/authz`. The `restapi/authz` in the URL should map to the `package` definition in the REGO file. 

To run OPA, you can run the following docker command and follow the logs,
```
docker run -i -t -p 8181:8181 --name opa -v /path/to/src/main/resources/api_authz.rego:/api_authz.rego openpolicyagent/opa:0.10.5 run --server --log-level=debug api_authz.rego
```

#### REGO Example (api_authz.rego)

REGO is a high-level declarative language for authoring policies. Here you can find a [REGO Tutorial](https://www.openpolicyagent.org/docs/get-started.html) and also you can play with REGO in the [Rego Playground](https://play.openpolicyagent.org/).

This access control checks if the request is a "GET" request to the "/users/roles" API from an authenticated request with its authorities contains "USER_QUERY_AUTH_ROLES".

```
package restapi.authz

allow {
  input.method = "GET"
  input.api = "/users/roles"
  input.auth.authorities[i].authority = "USER_QUERY_AUTH_ROLES"
}
```

### Access Control Matrix

There are two sets of access controls in place. First one is the OPA and the second is the @PreAuthorize annotations. OPA is evaluated first and only if OPA grants access, then @PreAuthorize annotation evaluated, below is the access matrix.

```
            1.OPA    2.PreInvocationAuthorizationAdviceVoter
granted      true    true
denied       true    false
denied       false   ---
```

### Requestbin

If you'd like to see the Register Request to the AMs(Authorization Microservice), you should start the `requestbin` using the below docker command, go to `http://localhost:8000` create a bin, and set the `ams.service.url` key in the `application.yaml` file. 

```
docker run -d --name requestbin -p 8000:8000 canmogol/requestbin:single-worker
```


### Postman

You can import the `JWT-Keycloak.postman_collection.json` file into Postman application and test the REST API. The `GET NEW TOKEN` request uses the `myra.manson` user which you need to create in Keycloak, or you can create any user in the `Demo` realm and use that user.
