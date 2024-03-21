<!-- Introduction -->
# java_ocr

Simple service that accepts images and performs OCR, sending the result of the operation to the client.

![image](https://github.com/kapiaszczyk/java_OCR/assets/41442206/b3df343b-a5cf-4a37-8733-6990ba03713e)

<!-- ABOUT -->
## About

The OCR (Optical Character Recognition) service utilizes the Tesseract library to extract text from images. It provides an API that is rate-limited based on API keys and user authority. The service logs operations and errors to both the console and a rolling file appender. API documentation is automatically generated using openAPI and can be accessed via the Swagger UI.

## Key Features
OCR functionality powered by the Tesseract library.
Rate limiting based on API keys to manage request quotas.
Comprehensive logging of operations and errors at different log levels.
Interactive API documentation through Swagger UI for easy exploration and testing of endpoints.

### Docker image
**You can find the docker image of the whole application [here](https://hub.docker.com/layers/kapiaszczyk/java_ocr/0.1.0/images/sha256-eed16d49f8bd1a458c1f966bb3c050da680fa54f05b89c3f711aaf9098d9fd81?context=repo).**

## Features

### Tesseract and Tess4J
Tess4J is a Java JNA (Java Native Access) wrapper for the Tesseract OCR API. Tesseract is an open-source text recognition (OCR) Engine available under the Apache 2.0 license. It performs the actual OCR reading.

### Rate Limiting
Rate limiting is implemented based on API keys. If a request does not include one, the limit is applied based on the IP it originated from. This method is not perfect since the application does not truly verify the key, and the IP cannot be trusted unless it comes from a trusted proxy.

This feature was implemented using the bucket4j library. Buckets are cached in memory.

### User registration
On startup, the application creates a superuser account. Requests authenticated with that account can create new admin accounts. This is managed by a custom user details service, and the credentials are stored in an H2 database.

To ensure a level of security, the passwords are hashed. However, this approach is not completely safe since superuser access is a single point of failure offering unlimited access to the actuator and registration endpoints. OCR requests coming from a superuser and admin are not rate-limited.

### Filters
To prevent invoking the controller and service for requests that cannot be processed, a file validation filter was implemented. The filter checks the request and file details, such as extension and content type. These checks are not perfect since the file can be carefully prepared and spoofed, but this approach is sufficient for this application.

### Spring Actuator
Information about the application can be accessed via Spring Actuator. Access to these endpoints is restricted to administrator and superuser accounts.

### Logging
Basic logging of errors and events is implemented using Logback via the slf4j facade. The logger logs human-readable statements to the console and JSON statements to a file. The file is rolled over daily or when it reaches a certain size. Archived logs are kept for a month and then deleted. Two filters were implemented to log some details of requests and responses. **Sensitive information like IP or API keys is hashed and never stored explicitly**.

### Spring AOP
For learning purposes, two different approaches were used to log the execution time of OCR reading and responding to requests. The first approach used Spring AOP PerformanceMonitor, and the other used a custom Aspect.

### Tests
Unit tests were implemented for both the controller (using Mockito to mock the service responses) and service, as well as testing the application end-to-end. Additional tests could be created to test the logging functionality, PerformanceMonitor, security, and so on.

## Running the application

#### Using the docker image
The easiest way to run the application is to use the docker image. The image is available on Docker Hub and can be pulled using the following command:
```
docker pull kapiaszczyk/java_ocr:latest
```

After pulling the image, the application can be run using the following command:
```
docker run -p 8080:8080 kapiaszczyk/java_ocr:latest
```

#### From source
To run the application from source, you need to have Java 21 and Maven installed. After cloning the repository, navigate to the root directory and run the following command:
```
mvn spring-boot:run
```

## Accessing the application
The application can be accessed via the following URL:
```
http://localhost:8080
```
The Swagger UI can be accessed via the following URL:
```
http://localhost:8080/swagger-ui.html
```

## Configuration
The application can be configured using the `application.properties` file. The following properties can be set:

| Name                                                         | Default Value | Description                                                                                         |
|--------------------------------------------------------------|---------------|-----------------------------------------------------------------------------------------------------|
| spring.servlet.multipart.max-file-size                       | 5MB           | Specifies the maximum size permitted for uploaded files. The default is 1MB.                        |
| spring.servlet.multipart.max-request-size                    | 5MB           | Specifies the maximum size allowed for multipart/form-data requests. The default is 10MB.             |
| logging.level.aop.com.kapia.ServicePerformanceMonitorInterceptor | TRACE      | Sets the level of logging for ServicePerformanceMonitorInterceptor to TRACE level.                  |
| ServicePerformanceMonitorInterceptor.override-logging-level  | INFO          | Manually overrides the logging level for ServicePerformanceMonitorInterceptor to INFO level.         |
| management.endpoints.web.exposure.include                    | *             | Exposes all endpoints for Spring Actuator.                                                          |
| su.username                                                  | superuser     | Specifies the username for the superuser.                                                           |
| su.password                                                  | superuser     | Specifies the password for the superuser.                                                           |
| spring.jpa.open-in-view                                      | false         | Disables open in view for JPA                                                      |
| spring.datasource.url                                        | jdbc:h2:mem:test | Specifies the URL for the data source using the H2 in-memory database for testing.                  |
| spring.datasource.driver-class-name                           | org.h2.Driver | Specifies the driver class name for the H2 database.                                                |
| tessdata.path                                                | tessdata      | Specifies the path for Tessdata.                                                                    |
| pricing.plans.free.limit.capacity                             | 10            | Specifies the capacity limit for the FREE pricing plan.                                              |
| pricing.plans.free.limit.tokens                               | 10            | Specifies the tokens limit for the FREE pricing plan.                                                |
| pricing.plans.free.refill.rate                                | 10            | Specifies the refill rate for the FREE pricing plan.                                                 |
| pricing.plans.basic.limit.capacity                            | 20            | Specifies the capacity limit for the BASIC pricing plan.                                             |
| pricing.plans.basic.limit.tokens                              | 20            | Specifies the tokens limit for the BASIC pricing plan.                                               |
| pricing.plans.basic.refill.rate                               | 20            | Specifies the refill rate for the BASIC pricing plan.                                                |
| pricing.plans.pro.limit.capacity                              | 30            | Specifies the capacity limit for the PRO pricing plan.                                               |
| pricing.plans.pro.limit.tokens                                | 30            | Specifies the tokens limit for the PRO pricing plan.                                                 |
| pricing.plans.pro.refill.rate                                 | 30            | Specifies the refill rate for the PRO pricing plan.                                                  |

Equivalent properties can be found in the application-test.properties.

## Future improvements
The application is a working prototype that is standalone and not suitable for real-world use (due to security and implementation of H2 database or in-memory cache). As of the current version, the following improvements could be made:

- Track user accounts in a separate database that will be the only source of truth for the application (since the database is in-memory, each instance of the application will have a different set of users).
- Use a separate cache service, like Redis, to store the buckets (again, the in-memory cache is not suitable for a real-world application).
- Resolve the pricing plan in a separate service allowing better flexibility.
- Process logs outside the application, using a separate service or tool.
- Offer OCR reading in multiple languages.

<!-- STACK -->
## Built With

[![bucket4j][bucket4j]][bucket4j-url]
[![H2][H2]][H2-url]
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
[H2]: https://img.shields.io/badge/H2-blue
[H2-url]: https://www.h2database.com/html/main.html
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
[Docker]: https://img.shields.io/badge/Docker-blue
[Docker-url]: https://www.docker.com/
[Maven]: https://img.shields.io/badge/Maven-blue
[Maven-url]: https://maven.apache.org/
[Postman]: https://img.shields.io/badge/Postman-orange
[Postman-url]: https://www.postman.com/
[Swagger]: https://img.shields.io/badge/Swagger-green
[Swagger-url]: https://swagger.io/
