# Start by building the application.
FROM gradle:jdk17-alpine  as build

WORKDIR /java/app
COPY . .

RUN gradle shadowJar

FROM gcr.io/distroless/java17-debian11:nonroot
COPY --from=build /java/app/build/libs/StonksBot-*.jar app.jar
# Copy custom aws-config
COPY aws-config.yaml aws-config.yaml
CMD ["app.jar"]