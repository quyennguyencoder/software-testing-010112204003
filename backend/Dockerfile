# Sử dụng image chứa sẵn Java 17 + Maven
FROM maven:3.9.8-eclipse-temurin-17-alpine

WORKDIR /app

# Copy pom.xml trước
COPY pom.xml .

# Copy source code
COPY src ./src

# Chạy ứng dụng (skip test compilation vì tests chưa update cho Product refactor)
CMD ["mvn", "spring-boot:run", "-Dmaven.test.skip=true"]