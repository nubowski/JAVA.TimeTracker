## User Controller

All User Controller API endpoints are prefixed with `/users`.

### Get All Users

This endpoint returns a list of all users.

Endpoint: `/`
Method: `GET`

Example: `curl -X GET http://localhost:8080/users`

<details>
<summary>Example Response:</summary>

```json
[
  {
    "id": 3702,
    "username": "test_user",
    "displayName": "nagibator9000",
    "email": "test@user.com",
    "createdAt": "2023-06-15T01:39:59.583057",
    "tasks": []
  },
  
  {
    "id": 1927,
    "username": "old_user",
    "displayName": "Old Ben",
    "email": "ke@nobi.com",
    "createdAt": "2022-03-15T13:32:13.192847",
    "tasks": []
  }
]
```

</details>

### Get User by Username

This endpoint returns the user associated with the provided username.

Endpoint: `/{username}`
Method: `GET`

Example: `curl -X GET http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "nagibator9000",
  "email": "test@user.com",
  "createdAt": "2023-06-15T01:39:59.583057",
  "tasks": [
    {
      "id": 0,
      "name": "task",
      "description": "just a task",
      "createdAt": "2023-06-14T22:53:31.174Z",
      "user": "test_user",
      "timeLogs": [
        {
          "id": 0,
          "startTime": "2023-06-14T22:53:31.174Z",
          "endTime": "2023-06-14T22:53:31.174Z",
          "endedByUser": true,
          "taskState": "ONGOING",
          "task": "task"
        }
      ]
    }
  ]
}
```

</details>

### Create User

This endpoint creates a new user with the provided details.

Endpoint: `/`
Method: `POST`
Body:
```json
{
  "username":"test_user",
  "email":"test@user.com",
  "displayName":"nagibator9000"
}
```

Example: `curl -X POST -H "Content-Type: application/json" -d '{"username":"test_user", "email":"test@user.com", "displayName":"nagibator9000"}' http://localhost:8080/users
`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "nagibator9000",
  "email": "test@user.com",
  "createdAt": "2023-06-15T01:39:59.5830572",
  "tasks": []
}
```

</details>

### Update User

This endpoint updates the user associated with the provided username. 
You can send `null` or empty string `""` it will not re-write the existing data.

Endpoint: `/{username}`
Method: `PUT`
Body:
```json
{
  "email": "user@test.com",
  "displayName": "Old Ben"
}
```

Example: `curl -X PUT -H "Content-Type: application/json" -d "{"email":"user@test.com", "displayName":"Old Ben"}" http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "Old Ben",
  "email": "user@test.com",
  "createdAt": "2023-06-15T01:39:59.583057",
  "tasks": []
}
```

</details>

### Delete User (Under Construction)

This endpoint is intended to delete the user associated with the provided username.
But saved archived tracker data.
However, it is currently under construction and may not function as expected.

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

OK

</details>

### Reset User's TimeLogs and Tasks

This endpoint resets the user's time logs and tasks associated with the provided username.

Endpoint: `/{username}/reset`
Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}/reset`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "Old Ben",
  "email": "user@test.com",
  "createdAt": "2023-06-15T01:39:59.583057",
  "tasks": []
}
```

</details>
