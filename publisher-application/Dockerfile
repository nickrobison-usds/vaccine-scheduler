FROM gradle:6.7.1-jdk11-hotspot

WORKDIR /home/gradle/publisher
COPY --chown=gradle:gradle ./ ./

RUN gradle --no-daemon classes testClasses assemble -p publisher-application

ENTRYPOINT ["gradle", "--info", "--no-daemon", "-p", "publisher-application"]
CMD ["tasks"]
