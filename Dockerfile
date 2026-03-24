# ====================================================
# Stage 1: Build the application
# ====================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ====================================================
# Stage 2: Run the application
# ====================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create uploads directory in /tmp (writable in containers)
RUN mkdir -p /tmp/uploads

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Hugging Face Spaces uses port 7860
EXPOSE 7860

# Run with the 'hf' profile and production settings
ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=hf", \
  "-jar", "app.jar"]
