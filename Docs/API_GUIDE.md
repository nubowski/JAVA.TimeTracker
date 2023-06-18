# API Usage Guide

This guide provides information on how to use the available API endpoints.

Quick tip. For all windows enjoyers when pasting the `curl` you should escape `"` inside a `json` (body) data:

`curl -X POST -H "Content-Type: application/json" -d '{"name": "task_name", "description": "some description"}' http://localhost:8080/tasks/test_user`

For windows would be like:

`curl -X POST -H "Content-Type: application/json" -d "{ \"name\": \"task_name\", \"description\": \"some description\" }" http://localhost:8080/tasks/test_user
`


## User Controller

All User Controller API endpoints are prefixed with `/users`.


### Get All Users

This endpoint returns a list of all users.

- Endpoint: `/`
- Method: `GET`

Example: `curl -X GET http://localhost:8080/users`

<details>
<summary>Example Response:</summary>

```json
[
  {
    "id": 3702,
    "username": "test_user",
    "email": "test@user.com",
    "displayName": "nagibator9000",
    "createdAt": "2023-06-15T01:39:59.583057"
  },
  
  {
    "id": 1927,
    "username": "old_user",
    "email": "ke@nobi.com",
    "displayName": "Old Ben",
    "createdAt": "2022-03-15T13:32:13.192847"
  }
]
```

</details>


### Get User by Username

This endpoint returns the user associated with the provided username.

- Endpoint: `/{username}`
- Method: `GET`

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

- Endpoint: `/`
- Method: `POST`
- Body:
```json
{
  "username":"test_user",
  "email":"test@user.com",
  "displayName":"nagibator9000"
}
```

Example: `curl -X POST -H "Content-Type: application/json" -d '{"username":"test_user", "email":"test@user.com", "displayName":"nagibator9000"}' http://localhost:8080/users`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "nagibator9000",
  "email": "test@user.com",
  "createdAt": "2023-06-15T01:39:59.5830572"
}
```

</details>

### Update User


This endpoint updates the user associated with the provided username. 
You can send `null` or empty string `""` it will not re-write the existing data.

- Endpoint: `/{username}`
- Method: `PUT`
- Body:
```json
{
  "email": "user@test.com",
  "displayName": "Old Ben"
}
```

Example: `curl -X PUT -H "Content-Type: application/json" -d '{"email":"user@test.com", "displayName":"Old Ben"}' http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 3702,
  "username": "test_user",
  "displayName": "Old Ben",
  "email": "user@test.com",
  "createdAt": "2023-06-15T01:39:59.583057"
}
```

</details>

### Delete User (Under Construction)


This endpoint is intended to delete the user associated with the provided username.
But saved and migrate archived tracker data.
However, it is currently under construction and may not function as expected.

- Endpoint: `/{username}`
- Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}`

<details>
<summary>Example Response:</summary>

NOT_IMPLEMENTED

</details>

### Delete User and their Tasks


This endpoint deletes the user and all their tasks associated with the provided username.

- Endpoint: `/{username}/delete`
- Method: `DELETE`

Example: `curl -X DELETE http://localhost:8080/users/{username}/delete`

<details>
<summary>Example Response:</summary>

OK

</details>

### Reset User's TimeLogs and Tasks


This endpoint resets the user's time logs and tasks associated with the provided username.

- Endpoint: `/{username}/reset`
- Method: `DELETE`

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

### Get TimeLogs by User and Date Range


This endpoint gets the time logs of a user within a certain date range.
You can also specify the sort order and the output format.

**sort**: way of sorting output (default value `duration`)

`start_time` - first by added to the tracker

`duration` - first by time spent

**output**: output format (default value `duration`)

`duration` - duration HH:mm format

`interval` - interval of date:time (under construction)

**date format** - `2023-06-18T15:30:00`  ISO 8601


- Endpoint: `/{username}/time_logs/date_range`
- Method: `GET`
- Example: `curl -X GET "http://localhost:8080/users/{username}/time_logs/date_range?start={start_date}&end={end_date}&sort={sort_order}&output={output_format}"`

- For intervals output: `curl -X GET "http://localhost:8080/users/test_user/time_logs/date_range?start=2023-06-17T12:00:00&end=2023-06-18T15:30:00&sort=duration&output=duration"`
- For durations output: `curl -X GET "http://localhost:8080/users/test_user/time_logs/date_range?start=2023-06-17T12:00:00&end=2023-06-18T15:30:00&sort=start_time&output=interval"`

<details>
<summary>Example Response:</summary>

```json
["2023-06-13 17:43 - 2023-06-13 22:00 | testTask1, 2023-06-13 02:43 - 2023-06-13 03:32 | testTask2"]
```

```json
["testTask1 - 04:17, testTask2 - 00:49"]
```

</details>

### Get Total Work Effort by User and Date Range


This endpoint calculates the total work effort of a user within a certain date range.

**date format** - `2023-06-18T15:30:00`  ISO 8601



- Endpoint: `/{username}/work_effort`
- Method: `GET`
- Example: `curl -X GET "http://localhost:8080/users/{username}/work_effort?start={start_date}&end={end_date}"`

<details>
<summary>Example Response:</summary>

```json
["HH:mm"]
```

</details>


## Task Controller

All Task Controller API endpoints are prefixed with `/tasks`.




### Get All Tasks


This endpoint returns a list of all tasks.

- Endpoint: `/`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/tasks`

<details>
<summary>Example Response:</summary>

```json
[
  {
    "id": 0,
    "name": "task1",
    "description": "string",
    "createdAt": "2023-06-15T23:02:46.114Z",
    "username": "username1"
  },
  {
    "id": 1,
    "name": "task2",
    "description": "string",
    "createdAt": "2023-06-15T23:02:46.114Z",
    "username": "username2"
  }
]
```

</details>

### Get Task by ID


This endpoint returns the task associated with the provided ID.

- Endpoint: `/{id}`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/tasks/{id}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 0,
  "name": "task_name",
  "description": "description here",
  "createdAt": "2023-06-15T19:27:39.302Z",
  "user": {
    "id": 0,
    "username": "test_user",
    "displayName": "Old Ben",
    "email": "keno@bi.com",
    "createdAt": "2023-06-15T19:27:39.302Z",
    "tasks": [
      "task_name"
    ]
  },
  "timeLogs": [
    {
      "id": 0,
      "startTime": "2023-06-15T19:27:39.302Z",
      "endTime": "2023-06-15T19:27:39.302Z",
      "endedByUser": true,
      "taskState": "ONGOING",
      "task": "task_name"
    }
  ]
}
```

</details>

### Create Task


This endpoint creates a new task with the provided details.
The task cannot live separately of the user.

- Endpoint: `/{username}`
- Method: `POST`
- Body: 
```json
{
  "name": "string",
  "description": "string"
}
```
- Example: `curl -X POST -H "Content-Type: application/json" -d '{"name": "task_name", "description": "some description"}' http://localhost:8080/tasks/{username}
  `

<details>
<summary>Example Response:</summary>

```json
{
  "id": 2752,
  "name": "task_name",
  "description": "some description",
  "createdAt": "2023-06-18T21:31:12.4491549",
  "username": "test_user"
}
```

</details>

### Update Task


This endpoint updates the task associated with the provided ID.

- Endpoint: `/{id}`
- Method: `PUT`
- Body:
```json
{
  "name": "string",
  "description": "string"
}
```
- Example: `curl -X PUT -H "Content-Type: application/json" -d '{"name": "task_name", "description": "some description"}' http://localhost:8080/tasks/{id}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 0,
  "name": "string",
  "description": "string",
  "createdAt": "2023-06-15T22:13:08.066Z",
  "username": "string"
}
```

</details>

### Delete Task


This endpoint deletes the task associated with the provided task ID.

- Endpoint: `/{id}`
- Method: `DELETE`
- Example: `curl -X DELETE http://localhost:8080/tasks/{id}`

<details>
<summary>Example Response:</summary>

OK

</details>

### Start Task by ID


This endpoint starts a time log associated with the provided task ID.
It starts a `new` time log. And marks a start timestamp.

- Endpoint: `/start/{taskId}`
- Method: `POST`
- Example: `curl -X POST http://localhost:8080/tasks/start/{taskId}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 1002,
  "startTime": "2023-06-16T04:30:29.7187791",
  "endTime": null,
  "endedByUser": false,
  "taskState": "ONGOING"
}
```

</details>

### Stop Task by ID


This endpoint stops a time log associated with the provided task ID.
It stops a time log. And marks an end timestamp.

- Endpoint: `/stop/{taskId}`
- Method: `POST`
- Example: `curl -X POST http://localhost:8080/tasks/stop/{taskId}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 1002,
  "startTime": "2023-06-16T04:30:29.718779",
  "endTime": "2023-06-16T04:32:58.4761401",
  "endedByUser": false,
  "taskState": "USER_STOPPED"
}
```

</details>

### Pause Task (Under Construction)


This endpoint pauses the task associated with the providedtask ID.
Working as expected, but do nothing more than left a Task status `PAUSED` for now.

- Endpoint: `/pause/{taskId}`
- Method: `POST`
- Example: `curl -X POST http://localhost:8080/tasks/pause/{taskId}`

<details>
<summary>Example Response:</summary>

OK

</details>

### Resume Task (Under Construction)


This endpoint resumes the task associated with the provided task ID.
Working as expected, but do nothing more than left a Task status `ONGOING` for now.

- Endpoint: `/resume/{taskId}`
- Method: `POST`
- Example: `curl -X POST http://localhost:8080/tasks/resume/{taskId}`

<details>
<summary>Example Response:</summary>

OK

</details>

### Get Task Time Elapsed (Under Construction)


This endpoint gets the elapsed time of the task associated with the provided task ID.
Only for internal use. Returning non formatted Duration.

- Endpoint: `/{taskId}/time_elapsed`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/tasks/{taskId}/time_elapsed`

<details>
<summary>Example Response:</summary>

```json
{
  "seconds": 0,
  "zero": true,
  "nano": 0,
  "negative": true,
  "positive": true,
  "units": [
    {
      "durationEstimated": true,
      "timeBased": true,
      "dateBased": true
    }
  ]
}
```

</details>





## TimeLog Controller

All TimeLog Controller API endpoints are prefixed with `/time_logs`.


### Get All TimeLogs (Under Construction)


This endpoint returns a list of all time logs.
Could be a lot of logs, so not recommended yet.
Will provide some request KEYS later on (like ?=statement,?=username and so on)

- Endpoint: `/`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/time_logs`

<details>
<summary>Example Response:</summary>

```json
[
  {
    "id": 0,
    "startTime": "2023-06-15T23:06:16.177Z",
    "endTime": "2023-06-15T23:06:16.177Z",
    "endedByUser": true,
    "taskState": "ONGOING",
    "task": {
      "id": 0,
      "name": "string",
      "description": "string",
      "createdAt": "2023-06-15T23:06:16.177Z",
      "user": {
        "id": 0,
        "username": "string",
        "displayName": "string",
        "email": "string",
        "createdAt": "2023-06-15T23:06:16.177Z",
        "tasks": [
          "string"
        ]
      },
      "timeLogs": [
        "string"
      ]
    }
  },
  {
    "id": 1,
    "startTime": "2023-06-15T23:06:16.177Z",
    "endTime": "2023-06-15T23:06:16.177Z",
    "endedByUser": true,
    "taskState": "ONGOING",
    "task": {
      "id": 0,
      "name": "string",
      "description": "string",
      "createdAt": "2023-06-15T23:06:16.177Z",
      "user": {
        "id": 0,
        "username": "string",
        "displayName": "string",
        "email": "string",
        "createdAt": "2023-06-15T23:06:16.177Z",
        "tasks": [
          "string"
        ]
      },
      "timeLogs": [
        "string"
      ]
    }
  }
]
```

</details>

### Get TimeLogs by Task ID


This endpoint returns a list of all time logs for the task associated with the provided task ID.

- Endpoint: `/task/{taskId}`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/time_logs/task/{taskId}`

<details>
<summary>Example Response:</summary>

```json
[
  {
    "id": 0,
    "startTime": "2023-06-18T19:17:23.404Z",
    "endTime": "2023-06-18T19:17:23.404Z",
    "endedByUser": true
  },
  {
    "id": 2,
    "startTime": "2023-06-18T19:17:23.404Z",
    "endTime": "2023-06-18T19:17:23.404Z",
    "endedByUser": false
  }
]
```

</details>

### Get TimeLog by ID


This endpoint returns the time log associated with the provided ID.

- Endpoint: `/{id}`
- Method: `GET`
- Example: `curl -X GET http://localhost:8080/time_logs/{id}`

<details>
<summary>Example Response:</summary>

```json
{
  "id": 0,
  "startTime": "2023-06-18T19:28:05.189Z",
  "endTime": "2023-06-18T19:28:05.190Z",
  "endedByUser": true,
  "taskName": "string"
}
```

</details>


### Delete TimeLogs by Task ID (Under Construction)


This endpoint deletes the time log associated with the provided ID.

- Endpoint: `/{id}`
- Method: `DELETE`
- Example: `curl -X DELETE http://localhost:8080/time_logs/{taskId}`

<details>
<summary>Example Response:</summary>

OK

</details>


### Delete TimeLog


This endpoint deletes the time log associated with the provided ID.

- Endpoint: `/{id}`
- Method: `DELETE`
- Example: `curl -X DELETE http://localhost:8080/time_logs/{id}`

<details>
<summary>Example Response:</summary>

OK

</details>



### Create TimeLog (Under Construction)


This endpoint creates a new time log with the provided details.
Currently, do not work and have only internal usage.

- Endpoint: `/`
- Method: `POST`
- Body: `{...}`
- Example: `curl -X POST -H "Content-Type: application/json" -d "{...}" http://localhost:8080/time_logs`

<details>
<summary>Example Response:</summary>

```json
 {
    "id": 0,
    "startTime": "2023-06-15T23:29:42.597Z",
    "endTime": "2023-06-15T23:29:42.597Z",
    "endedByUser": true,
    "taskState": "ONGOING",
    "task": {
      "id": 0,
      "name": "string",
      "description": "string",
      "createdAt": "2023-06-15T23:29:42.597Z",
      "user": {
        "id": 0,
        "username": "string",
        "displayName": "string",
        "email": "string",
        "createdAt": "2023-06-15T23:29:42.597Z",
        "tasks": [
          "string"
        ]
      },
      "timeLogs": [
        "string"
      ]
    }
  }
```

</details>