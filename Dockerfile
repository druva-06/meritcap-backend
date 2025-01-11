# Use Maven to build the application
FROM maven:3.8.4-openjdk-17 as builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code to the container
COPY pom.xml .
COPY src /app/src

# Build the application (package the JAR file)
RUN mvn clean package -DskipTests

# Use OpenJDK to run the application
FROM openjdk:17

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder image
COPY --from=builder /app/target/*.jar /app/cap.jar
# Expose the port your Spring Boot application runs on
EXPOSE 8080

# Command to run the JAR file
CMD ["java", "-jar", "/app/cap.jar"]