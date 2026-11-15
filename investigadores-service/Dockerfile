# Etapa 1: compilar con el wrapper de Maven.
# Se copian primero solo los archivos de build para aprovechar la cache de capas:
# mientras el pom no cambie, la descarga de dependencias no se repite.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# Etapa 2: imagen final, solo con el JRE y el jar.
# No lleva Maven, ni el codigo fuente, ni el JDK: pasa de ~600 MB a ~250 MB.
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin rica
USER rica

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
