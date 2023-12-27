# Start by building the application.
FROM gradle:8.5-jdk21-alpine  as build

WORKDIR /java/app
COPY . .

RUN gradle shadowJar

FROM gcr.io/distroless/java21-debian12:nonroot
COPY --from=build /java/app/build/libs/*-all.jar app.jar
# Copy custom aws-config
COPY config.yaml config.yaml
CMD ["app.jar"]