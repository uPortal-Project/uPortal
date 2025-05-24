# uPortal OIDC and OAuth 2.0 Implementation Documentation
---
## OIDC Endpoints in uPortal

### 1. Authorization Endpoint
- **Purpose**: Initiates the OIDC flow by redirecting users to uPortal’s login page while including custom claims relevant to the application.
- **HTTP Method**: `GET`
- **Example Request**:
    ```
    GET /uPortal/api/v5-1/oauth/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope=openid&state={state}&claims={custom_claims}
    ```
- **Parameters**:
    - `response_type`: Must be `code`.
    - `client_id`: Unique identifier for the client application registered in uPortal.
    - `redirect_uri`: URI to which uPortal redirects after user authorization.
    - `scope`: Must include `openid` and any additional scopes required by the application.
    - `state`: Opaque value used to maintain state between request and callback.
    - `claims`: Custom claims required for the user (see **Custom Claims** section for examples).

### 2. Token Endpoint
- **Purpose**: Exchanges the authorization code for an access token and an ID token containing uPortal-specific custom claims.
- **HTTP Method**: `POST`
- **Example Request**:
    ```
    POST /uPortal/api/v5-1/oauth/token
    Content-Type: application/x-www-form-urlencoded

    grant_type=authorization_code&code={authorization_code}&redirect_uri={redirect_uri}&client_id={client_id}&client_secret={client_secret}
    ```
- **Parameters**:
    - `grant_type`: Must be `authorization_code`.
    - `code`: The authorization code received from the authorization endpoint.
    - `redirect_uri`: Must match the URI used in the authorization request.
    - `client_id`: The application's client ID.
    - `client_secret`: Secret provided during client registration.

### 3. UserInfo Endpoint
- **Purpose**: Retrieves user information using the access token, including uPortal's custom claims.
- **HTTP Method**: `GET`
- **Example Request**:
    ```
    GET /uPortal/api/v5-1/oauth/userinfo
    Authorization: Bearer {access_token}
    ```
- **Headers**:
    - `Authorization`: Bearer token obtained from the token endpoint.
- **Response**: JSON object containing standard OIDC claims and uPortal-specific custom claims.

### 4. End Session Endpoint
- **Purpose**: Logs the user out of the uPortal OIDC session and optionally redirects them to a specified URL.
- **HTTP Method**: `GET`
- **Example Request**:
    ```
    GET /uPortal/api/v5-1/oauth/logout?post_logout_redirect_uri={redirect_uri}&id_token_hint={id_token}
    ```
- **Parameters**:
    - `post_logout_redirect_uri`: URI to redirect the user after logout.
    - `id_token_hint`: ID token used to identify the session being logged out.

## Custom Claims in uPortal

uPortal supports custom claims, allowing applications to access additional user attributes stored within the uPortal user repository. These custom claims enhance the standard OIDC claims and provide context specific to the application’s needs.

### Defining Custom Claims
Custom claims can be defined to represent user-specific attributes, such as roles or affiliations. Here’s how to configure them in uPortal:

- **Example Custom Claims Request**:
    ```json
    {
        "userinfo": {
            "user_id": { "essential": true },
            "roles": { "values": ["student", "faculty"] },
            "department": { "values": ["Engineering", "Business"] }
        }
    }
    ```

### Claim Mapping
Custom claims are mapped to user attributes in uPortal, allowing these claims to be included in the ID token and the `/userinfo` response. 

- **Example Claim Mapping**:
    ```json
    {
        "user_id": "uid",
        "roles": "userRoles",
        "department": "userDepartment"
    }
    ```

### Retrieving Custom Claims
When a client application calls the `/userinfo` endpoint, the response includes both standard claims and any custom claims configured in uPortal.

- **Example Response**:
    ```json
    {
        "sub": "1234567890",
        "name": "Jane Doe",
        "email": "jane.doe@university.edu",
        "user_id": "janedoe123",
        "roles": ["student", "member"],
        "department": "Engineering"
    }
    ```

## Three-Legged OAuth 2.0 Flow in uPortal

The three-legged OAuth 2.0 flow allows third-party applications to securely access user data while accommodating custom claims.

### Flow Steps

1. **User Authorization**:
   - Redirect the user to uPortal’s authorization endpoint with necessary custom claims.
   
   **Example Request**:
    ```
    GET /uPortal/api/v5-1/oauth/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}&scope=openid&claims={"userinfo":{"user_id":{"essential":true},"roles":{"values":["student","faculty"]}}}
    ```

2. **Token Exchange**:
   - The client exchanges the authorization code for an access token and ID token, which includes the requested custom claims.
   
   **Example Request**:
    ```
    POST /uPortal/api/v5-1/oauth/token
    Content-Type: application/x-www-form-urlencoded

    grant_type=authorization_code&code={authorization_code}&redirect_uri={redirect_uri}&client_id={client_id}&client_secret={client_secret}
    ```

3. **Accessing User Information**:
   - The client can use the access token to call uPortal's `/userinfo` endpoint to retrieve user information, including custom claims.
   
   **Example Request**:
    ```
    GET /uPortal/api/v5-1/oauth/userinfo
    Authorization: Bearer {access_token}
    ```

## Security Considerations
- **Token Expiration**: Ensure tokens are managed appropriately to maintain user sessions without exposing sensitive data.
- **Custom Claims Security**: Properly validate and manage custom claims to prevent unauthorized access to sensitive user information.
- **Secure Storage**: Client secrets must be stored securely to protect against unauthorized access.

## References
- [uPortal Documentation](https://uportal-project.github.io/uPortal/)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [OAuth 2.0 Authorization Framework](https://oauth.net/2/)
