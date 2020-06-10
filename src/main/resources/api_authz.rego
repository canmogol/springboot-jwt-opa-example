package restapi.authz

allow {
  input.method = "GET"
  input.api = "/users/roles"
  input.auth.authorities[i].authority = "USER_QUERY_AUTH_ROLES"
}

allow {
  input.method = "GET"
  input.api = "/users/username"
}

