# Technologies

## Backend

* Java 21
* Maven (I use 3.9.15)
* Spring Boot 4.0.6
* Spring MVC
* Spring Data JPA
* Hibernate
* Docker/Dockerfile
* Spring Cloud OpenFeign

## Database

* PostgreSQL 16

## Validation & Documentation

* Jakarta Bean Validation
* Swagger - http://localhost:8080/swagger-ui.html

## Monitoring

* Spring Boot Actuator
* Micrometer
* Prometheus - http://localhost:8080/actuator/prometheus

## Testing

* JUnit
* Mockito
* Testcontainers
* JaCoCo

## Code Quality

* Spotless
* Google Java Format

  -----------------------------------------------------------------------------------------------

## An application for creating and using discount coupons - problem to solve

While developing the application, the main problem I considered was the parallel use of the same code by multiple users simultaneously. Negative consequences of a poorly implemented solution could include excessive use of the maximum number of coupons, incorrect coupon assignment to a user in the event of a high frequency of coupon redemption requests, and poor application performance (for a very large system, e.g., 100,000 simultaneous requests using the same discount coupon).

To solve the problem mentioned above, I decided to utilize the concept of atomicity. This solution has its pros and cons, but the more I read the recruitment task, the more I concluded that this approach would be the best solution for this type of application. I also didn't want to overdo it with an overly complex solution (I assumed that the same discount code would not be used more than 1,000-5,000 times at the same time = 1,000-5,000 requests using the same coupon simultaneously). In my opinion, this solution is ideal for this type of approach.

The advantage of the current solution I've developed is that we eliminate the concept of race conditions. We guarantee that everything will be performed as planned, including assigning coupons to specific users and generating discount coupons. The downside of my solution is that it creates a bottleneck on the database record. The more requests for the same discount code, the more the application will lose performance. However, the real performance loss should only be felt when there are 10,000+ requests for the same discount coupon.

I've read that large companies/corporations use the tokenization approach for their applications. A discount coupon, e.g., SPRING2026, is created for 100,000 users and stored in the database as

1) SPRING2026-000001

2) SPRING2026-000002

etc....

This solution is particularly effective because we have 100,000 records for the same discount code in the table. When a user uses a coupon, they query the application, and the application searches the table for the associated discount code. If one isn't available, the appropriate information is returned. This is certainly a better approach because it doesn't lock a specific record in the database, but as I mentioned, I didn't want to overengineer and over-implement (as I mentioned, this solution is for much more advanced systems – and the task text allowed for flexibility).

-----------------------------------------------------------------------------------------------------------------------

## How to run an application on Docker

Using a window in intellij (it should also work in a regular terminal) on the main root of the project, execute the command:
- mvn clean package
- docker-compose up --build

This should build the Docker image and run our application. After this, we can check if Swagger is working:
- http://localhost:8080/swagger-ui/index.html

-------------------------------------------------------------------------------------------------------------------------
## Coupon app - functionality

The application has two main endpoints: one for creating coupons and the other for redeeming coupons. These endpoints are located in Swager on specific paths. I used the Postman application for my testing.

# For the endpoint using IP verification, I used IP API. It's a free tool for checking IP locations.

Here's the website and the endpoint it uses for IP verification:

https://ip-api.com/

# Profiles

Since I was testing the application on localhost, I didn't have a normal IP address. I decided to add profiles for the application: local and prod. For local, I assumed the IP was verified without the API IP and the country returned was "PL." Assuming the application was in production and had its own server to which requests were made, clients would have their own dedicated IP address.

By default, the application is set to the local profile (we skip the API AP verification – if we want to change this, we must test the application somewhere other than localhost and set the application profile to prod).

-------------------------------------------------------------------------------------------------------------------------

# Metrics

I've also added a few metrics to the app, both baseline and custom (for demonstration purposes). This is also a good feature of a proper app. :) They can be found in the code and should be available after launching the app at the following URL: 
http://localhost:8080/actuator/metrics

-------------------------------------------------------------------------------------------------------------------------

# Tests

Both unit and integration tests have been added to the application. I decided to use TestContainers for integration testing. I expose the database in the container, which I use to test my code against a database instance. The code currently has 27 tests(combine integration and unit tests).

Currently, the most important test verifying the atomicity of my solution is located in the integration package, test section. It is called: shouldAllowOnlyMaxUsage_whenMultipleUsersUseCouponSimultaneously.

# I invite you to take a deeper look at this test, as it is one of the most important tests :)

-------------------------------------------------------------------------------------------------------------------------

# Jacoco

Jacoco has been added to the application, generating a test report after running: mvn clean package

I run it with the command: xdg-open target/site/jacoco/index.html

Currently, the application has a test coverage of 92%.

-------------------------------------------------------------------------------------------------------------------------

# CI/CD

I've also added a pipeline to the app (very preliminary). It only verifies the build and tests. In a production app, this pipeline can be scaled with additional features, such as auto-deployment to the environment. It can be found here on GitHub in the Actions section.

--------------------------------------------------------------------------------------------------------------------------

## Application functionality(apart from the way coupons are distributed)

- duplicate coupon verification
- maximum number of uses for a given coupon
- coupon usage, e.g., with the code "PL," is limited to users from that country
- feedback if the coupon has already been used, the coupon no longer exists, or the user has already used their coupon.

# I added some photos to the photos folder from Postman during my local tests. Check them out :)







