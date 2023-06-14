## User Controller

All User Controller API endpoints are prefixed with `/users`.

### Get All Users

This endpoint returns a list of all users.

Endpoint: `/`
Method: `GET`

Example: `curl -X GET http://localhost:8080/users`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Get User by Username

This endpoint returns the user associated with the provided username.

Endpoint: `/{username}`
Method: `GET`

Example: `curl -X GET http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Create User

This endpoint creates a new user with the provided details.

Endpoint: `/`
Method: `POST`
Body: `{...}`

Example: `curl -X POST -H "Content-Type: application/json" -d "{...}" http://localhost:8080/users`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Update User

This endpoint updates the user associated with the provided username.

Endpoint: `/{username}`
Method: `PUT`
Body: `{...}`

Example: `curl -X PUT -H "Content-Type: application/json" -d "{...}" http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Delete User

This endpoint deletes the user associated with the provided username.

Endpoint: `/{username}`
Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Delete User and their Tasks

This endpoint deletes the user and all their tasks associated with the provided username.

Endpoint: `/{username}/delete`
Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}/delete`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>

### Reset User's TimeLogs and Tasks

This endpoint resets the user's time logs and tasks associated with the provided username.

Endpoint: `/{username}/reset`
Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}/reset`

<details>
<summary>Example Response:</summary>

RESPONSE_PLACEHOLDER

</details>
