# ---- Étape 1 : build avec Maven (pas besoin de mvnw) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances : on copie d'abord uniquement le pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Puis le code source
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Étape 2 : image d'exécution légère ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Utilisateur non-root
RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
