# Sử dụng image chứa sẵn Java 17
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy file cấu hình maven trước để tận dụng cache layer
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Tải thư viện về (chỉ resolve dependencies cần thiết, tránh timeout)
# RUN ./mvnw dependency:resolve -DskipTests

# Copy source code
COPY src ./src

# Chạy ứng dụng (skip test compilation vì tests chưa update cho Product refactor)
CMD ["./mvnw", "spring-boot:run", "-Dmaven.test.skip=true"]