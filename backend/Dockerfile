# Stage 1: Build ứng dụng bằng Maven & Java 17
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml và tải dependencies trước để tận dụng Docker layer cache
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code và build file .jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Chạy ứng dụng với JRE 17 gọn nhẹ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy file jar từ Stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]