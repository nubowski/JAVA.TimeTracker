# Time Tracker Application

This application is designed to help manage and track time spent on tasks. It is built using Spring Boot and follows the Spring MVC pattern for clear separation of concerns.

## Features

1. **User Management:** Register new users, delete users, and retrieve user details.
2. **Task Management:** Create new tasks, delete tasks, and retrieve task details.
3. **TimeLog Management:** Record time spent on tasks, delete time logs, and retrieve details about time logs.

## Documentation

For more detailed information about the API and how to use it, please refer to our [API Guide](https://github.com/nubowski/JAVA.TimeTracker/blob/master/Docs/API_GUIDE.md)

## How to Run


1. Install Docker: You would need to have [Docker](https://docs.docker.com/get-docker/) installed on your machine to run the application. 
2. Get the last [snapshot](https://github.com/nubowski/JAVA.TimeTracker/tags) and download it.
3. Change properties if needed in `docker-compose.yml`.
4. Unpack it and `cmd>` root `docker compose up -d` or click the `lazy-button.bat`
5. Follow the [API guide](https://github.com/nubowski/JAVA.TimeTracker/blob/master/Docs/API_GUIDE.md) provided.

<details>
<summary>Have a testing field tho:</summary>

`94.19.184.24:23322/swagger-ui/index.html`
and the same `IP` and `port` for the playground
server could be offline, just in case

</details>


## License

[MIT](https://choosealicense.com/licenses/mit/)
