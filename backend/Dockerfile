# Sử dụng image chứa sẵn Java 17
FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

# Copy only pom.xml first to cache dependency resolution
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:resolve

# Copy source code
COPY src ./src

# Run the Spring Boot app without tests
CMD ["mvn", "spring-boot:run", "-Dmaven.test.skip=true"]