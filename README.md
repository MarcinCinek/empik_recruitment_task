# Technologies

## Backend

* Java 21
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
* Swagger / OpenAPI (`springdoc-openapi`) - http://localhost:8080/swagger-ui.html

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

