# OpenID Connect (OIDC) Implementation for uPortal

## Introduction
OpenID Connect (OIDC) is an authentication layer built on top of OAuth 2.0, enabling clients to verify user identities and obtain basic profile information. This documentation outlines the OIDC possibilities, including available endpoints, group and claims filters, and using OAuth endpoints for clients.

## OIDC Endpoints

### 1. Authorization Endpoint
- **Purpose**: Initiates the OIDC flow by redirecting users to the login page.
- **HTTP Method**: GET
- **Example**: 
    ```
    GET /authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope={scope}&state={state}
    ```
- **Parameters**:
    - `response_type`: The response type, e.g., `code`.
    - `client_id`: The client identifier.
    - `redirect_uri`: The redirection URI.
    - `scope`: The scope of the access request.
    - `state`: An opaque value used to maintain state between the request and callback.

### 2. Token Endpoint
- **Purpose**: Exchanges the authorization code for an access token.
- **HTTP Method**: POST
- **Example**: 
    ```
    POST /token
    Content-Type: application/x-www-form-urlencoded
    {
        "grant_type": "authorization_code",
        "code": "{authorization_code}",
        "redirect_uri": "{redirect_uri}",
        "client_id": "{client_id}",
        "client_secret": "{client_secret}"
    }
    ```
- **Parameters**:
    - `grant_type`: The type of the grant, e.g., `authorization_code`.
    - `code`: The authorization code received from the authorization server.
    - `redirect_uri`: The redirection URI used in the initial request.
    - `client_id`: The client identifier.
    - `client_secret`: The client secret.

### 3. UserInfo Endpoint
- **Purpose**: Retrieves user information based on the access token.
- **HTTP Method**: GET
- **Example**: 
    ```
    GET /userinfo
    Authorization: Bearer {access_token}
    ```
- **Headers**:
    - `Authorization`: The access token obtained from the token endpoint.

### 4. End Session Endpoint
- **Purpose**: Logs the user out of the OIDC provider.
- **HTTP Method**: GET
- **Example**: 
    ```
    GET /logout?post_logout_redirect_uri={redirect_uri}&id_token_hint={id_token}
    ```
- **Parameters**:
    - `post_logout_redirect_uri`: The URI to redirect to after logout.
    - `id_token_hint`: The ID token previously issued to the user.

## Group and Claims Filters

### Group Filters
- **Description**: Specify which user groups are included in the OIDC token.
- **Example**: 
    ```json
    "groups": ["admin", "user"]
    ```

### Claims Filters
- **Description**: Define which claims are returned in the ID token or access token.
- **Example**: 
    ```json
    "claims": {
        "email": { "essential": true },
        "role": { "values": ["admin", "user"] }
    }
    ```

## Using OAuth for Clients

### Client Registration
- **Process**: Clients must register with the OIDC provider to obtain a client ID and secret.
- **Steps**:
    1. Access the client registration endpoint.
    2. Provide required details such as client name and redirect URIs.
    3. Obtain the client ID and secret.

### Scopes and Permissions
- **Definition**: Scopes determine the level of access requested by the client.
- **Example**: 
    ```json
    "scope": "openid profile email"
    ```
- **Common Scopes**:
    - `openid`: Required to perform OIDC authentication.
    - `profile`: Requests access to the user's profile information.
    - `email`: Requests access to the user's email address.

### Token Retrieval Process
- **Steps**: Clients obtain access tokens to access protected resources.
- **Example**: 
    ```
    POST /token
    Content-Type: application/x-www-form-urlencoded
    {
        "grant_type": "client_credentials",
        "client_id": "{client_id}",
        "client_secret": "{client_secret}",
        "scope": "{scope}"
    }
    ```
- **Parameters**:
    - `grant_type`: The type of the grant, e.g., `client_credentials`.
    - `client_id`: The client identifier.
    - `client_secret`: The client secret.
    - `scope`: The scope of the access request.

## Additional Security Considerations
- **Token Expiration**: Always check the expiration time of tokens and renew them as needed.
- **Revocation**: Implement token revocation to invalidate tokens when necessary.
- **Secure Storage**: Store client secrets and tokens securely to prevent unauthorized access.

## References
- [OIDC Official Documentation](https://openid.net/developers/specs/)
- [OAuth 2.0 Specification](https://oauth.net/2/)
- [uPortal Documentation](https://apereo.github.io/uPortal/)

## Conclusion
This documentation serves as a comprehensive guide for developers to understand and implement OIDC using OAuth 2.0 in uPortal, facilitating secure user authentication and resource access.
