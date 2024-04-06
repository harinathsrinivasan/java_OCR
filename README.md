<!-- Introduction -->
# java_ocr

Simple service that accepts images and performs OCR, sending the result of the operation to the client.

![image](https://github.com/kapiaszczyk/java_OCR/assets/41442206/b3df343b-a5cf-4a37-8733-6990ba03713e)

<!-- ABOUT -->
## About

The OCR (Optical Character Recognition) service utilizes the Tesseract library to extract text from images. It provides an API that is rate-limited based on API keys and user authority. The service logs operations and errors to both the console and a rolling file appender. API documentation is automatically generated using openAPI and can be accessed via the Swagger UI.

## Key Features
- OCR functionality powered by the Tesseract library.
- Rate limiting based on API keys to manage request quotas stored in Redis.
- Comprehensive logging of operations and errors at different log levels.
- Interactive API documentation through Swagger UI for easy exploration and testing of endpoints.

## Docker image
**You can find the docker image of the whole application [here](https://hub.docker.com/layers/kapiaszczyk/java_ocr/0.2.0/images/sha256-df24b1a27c906c81c9719416bdd33819c95f4b9aa68655fbd5965d5c3eafe025?context=repo).**

## Features

### Tesseract and Tess4J
Tess4J is a Java JNA (Java Native Access) wrapper for the Tesseract OCR API. Tesseract is an open-source text recognition (OCR) Engine available under the Apache 2.0 license. It performs the actual OCR reading.

### Rate Limiting
Rate limiting is implemented based on API keys. If a request does not include one, the limit is applied based on the IP it originated from.

This feature was implemented using the bucket4j library. Buckets are stored in Redis instance.

### User registration
On startup, the application creates a superuser account. Requests authenticated with that account can create new admin accounts. This is managed by a custom user details service, and the credentials are stored in an MariaDB database.

To ensure a level of security, the passwords are hashed. Requests authenticated as coming from a superuser or admin users are not rate-limited.

### Filters
To prevent invoking the controller and service for requests that cannot be processed, a file validation filter was implemented. The filter checks the request and file details, such as extension and content type.

### Spring Actuator
Information about the application can be accessed via Spring Actuator. Access to these endpoints is restricted to administrator and superuser accounts.

### Logging
Basic logging of errors and events is implemented using Logback via the slf4j facade. The logger logs human-readable statements to the console and JSON statements to a file. The file is rolled over daily or when it reaches a certain size. Archived logs are kept for a month and then deleted. Two filters were implemented to log some details of requests and responses. **Sensitive information like IP or API keys is encrypted and never stored explicitly**.

### Spring AOP
For learning purposes, two different approaches were used to log the execution time of OCR reading and responding to requests. The first approach used Spring AOP PerformanceMonitor, and the other used a custom Aspect.

## Running the application

Since this version of application relies on Redis instance connection and a database connection, the best way to run the whole service is using Docker Compose. The compose file is included in the repository.

## Accessing the application
The application can be accessed via the following URL:
```
http://localhost:8080
```

The following endpoints are available:

| Endpoint            | Description                                                                |
|---------------------|----------------------------------------------------------------------------|
|`/getOCR`            | OCR service endpoint.                                                      |
|`/key`               | Key creation enpoint.                                                      |
|`/register`          | Internal endpoint allowing creation of admin accounts via the application. |


Full documentation is created by OpenApi and available under the `/swagger-ui.html` or `/v3/api-docs`.

## Configuration
The application is stored in the `application.properties` file. For deploying with docker, consider setting the following properties:

| Name                    | Corresponding property name            | Default value     | Description                                              |
| ----------------------- | -------------------------------------- | ----------------- | -------------------------------------------------------- |
| `SU_USERNAME`           | `su.username`                          | superuser         | Username for the superuser.                              |
| `SU_PASSWORD`           | `su.password`                          | superuser         | Password for the superuser.                              |
| `KEY_SALT`              | `key.salt`                             | ocr               | Salt used in hashing passwords.                          |
| `DB_HOST`               | `spring.datasource.url`                | localhost         | Database hostname.                                       |
| `DP_PORT`               | `spring.datasource.url`                | 3306              | Database port.                                           |
| `DB_NAME`               | `spring.datasource.url`                | users_credentials | Database name.                                           |
| `DB_USERNAME`           | `spring.datasource.username`           | ocr               | Username of database user.                               |
| `DB_PASSWORD`           | `spring.datasource.username`           | password          | Password of database user/                               |
| `REDIS_BUCKET_HOST`     | `redis.bucket.host`                    | localhost         | Redis hostname.                                          |
| `REDIS_BUCKET_PORT`     | `redis.bucket.port`                    | 6379              | Redis port.                                              |
| `REDIS_BUCKET_PASSWORD` | `redis.bucket.password`                | (none)            | Password used in creating connection with Redis.         |
| `REDIS_KEY_HOST`        | `redis.key.host`                       | localhost         | Redis hostname.                                          |
| `REDIS_KEY_PORT`        | `redis.key.port`                       | 6380              | Redis port.                                              |
| `REDIS_KEY_PASSWORD`    | `redis.key.password`                   | (none)            | Password used in creating connection with Redis.         |
| `PRICING_*_CAPACITY`    | `pricing.plans.*.capacity`             | 10/20/30          | Capacity of a bucket for a given plan.                   |
| `PRICING_*_TOKENS`      | `pricing.plans.*.tokens`               | 10/20/30          | Amount of tokens to add after the specified time period. |
| `PRICING_REFILL_RATE`   | `pricing.plans.refill.rate.in.minutes` | 60                | Token refill rate period.                                |

## Reliability
Layers of the application (OCR services, registration services, key generation services etc.) were both unit and integration tested with help of Mockito and Testcontainers. Additionally, endpoints were tested using Postman to verify expected behavior.

## Future improvements

The application is a working prototype that is standalone and not suitable for real-world use. As of the current
version, the following improvements could be made:

- Resolving the pricing plans in a separate service
- Processing logs outside the application
- Offering OCR reading in multiple languages
- Delegating the creation of the superuser to the database
- Replacing basic auth with JWT authentication.

### Security considerations

- Rate limiting IP requests is not reliable, since the IP might be forged
- In logs, both IP addresses and keys are hashed to prevent leaking sensitive data
- API keys and passwords are not stored in plain text, but encrypted
- Unauthorized access to superuser account is a security risk
- Secure the application with HTTPS and enable CSRF protection.

<!-- STACK -->

## Built With

[![bucket4j][bucket4j]][bucket4j-url]
[![MariaDB][MariaDB]][MariaDB-url]
[![Redis][Redis]][Redis-url]
[![Commons Validator][Commons Validator]][Commons Validator-url]
[![Tess4J][Tess4J]][Tess4J-url]
[![Log4j][Log4j]][Log4j-url]
[![Springdoc][springdoc]][Springdoc-url]
[![Spring Actuator][Spring Actuator]][Spring Actuator-url]
[![Spring AOP][Spring AOP]][Spring AOP-url]
[![Spring Data JPA][Spring Data JPA]][Spring Data JPA-url]
[![Spring Security][Spring Security]][Spring Security-url]
[![Spring Web][Spring Web]][Spring Web-url]
[![Maven][Maven]][Maven-url]
[![Postman][Postman]][Postman-url]
[![Docker][Docker]][Docker-url]
[![Swagger][Swagger]][Swagger-url]

<!-- MARKDOWN LINKS & IMAGES -->

[bucket4j]: https://img.shields.io/badge/bucket4j-orange
[bucket4j-url]: https://github.com/bucket4j/bucket4j
[MariaDB]: https://img.shields.io/badge/MariaDB-blue
[MariaDB-url]: https://mariadb.org/
[Redis]: https://img.shields.io/badge/redis-red
[Redis-url]: https://redis.io/
[Commons Validator]: https://img.shields.io/badge/Commons_Validator-blue
[Commons Validator-url]: https://commons.apache.org/proper/commons-validator/
[Tess4J]: https://img.shields.io/badge/Tess4J-blue
[Tess4J-url]: https://tess4j.sourceforge.net/
[Log4j]: https://img.shields.io/badge/Log4j-blue
[Log4j-url]: https://logging.apache.org/log4j/
[Springdoc]: https://img.shields.io/badge/Springdoc-green
[Springdoc-url]: https://springdoc.org/
[Spring Actuator]: https://img.shields.io/badge/Spring_Boot_Actuator-green
[Spring Actuator-url]: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
[Spring AOP]: https://img.shields.io/badge/Spring_Boot_AOP-green
[Spring AOP-url]: https://docs.spring.io/spring-framework/reference/core/aop.html
[Spring Data JPA]: https://img.shields.io/badge/Spring_Boot_Data_JPA-green
[Spring Data JPA-url]: https://docs.spring.io/spring-data/jpa/reference/index.html
[Spring Security]: https://img.shields.io/badge/Spring_Boot_Security-green
[Spring Security-url]: https://docs.spring.io/spring-security/reference/index.html
[Spring Web]: https://img.shields.io/badge/Spring_Boot_Web-green
[Spring Web-url]: https://docs.spring.io/spring-framework/reference/web.html
[Mockito]: https://img.shields.io/badge/Mockito-green
[Mockito-url]: https://site.mockito.org/
[Testcontainers]: https://img.shields.io/badge/Testcontainers-blue
[Testcontainers-url]: https://testcontainers.com/
[Docker]: https://img.shields.io/badge/Docker-blue
[Docker-url]: https://www.docker.com/
[Maven]: https://img.shields.io/badge/Maven-blue
[Maven-url]: https://maven.apache.org/
[Postman]: https://img.shields.io/badge/Postman-orange
[Postman-url]: https://www.postman.com/
[Swagger]: https://img.shields.io/badge/Swagger-green
[Swagger-url]: https://swagger.io/
