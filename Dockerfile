FROM oracle/graalvm-ce:19.3.0-java8 as graalvm
#FROM oracle/graalvm-ce:19.3.0-java11 as graalvm # For JDK 11
COPY . /home/app/todddo-openapi-java
WORKDIR /home/app/todddo-openapi-java
RUN gu install native-image
RUN native-image --no-server --static -cp build/libs/todddo-openapi-java-*-all.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/todddo-openapi-java/todddo-openapi-java /app/todddo-openapi-java
ENTRYPOINT ["/app/todddo-openapi-java", "-Djava.library.path=/app"]
