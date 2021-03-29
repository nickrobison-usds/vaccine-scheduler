FROM gradle:6.7.1-jdk11-hotspot

WORKDIR /home/gradle/api
COPY --chown=gradle:gradle ./ ./

RUN gradle --no-daemon classes testClasses assemble -p api-application

ENTRYPOINT ["gradle", "--info", "--no-daemon", "-p", "api-application"]
CMD ["tasks"]
