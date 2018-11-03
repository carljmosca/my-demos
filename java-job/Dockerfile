FROM java:8

COPY target/openshift-job-demo-1.0-SNAPSHOT.jar /app.jar

RUN groupadd --gid 1001 sboot
RUN useradd --uid 1001 -g sboot sboot

RUN chmod g=u /etc/passwd
RUN mkdir -p /home/sboot
RUN chown -R 1001:0 /home/sboot

ENV JAVA_OPTS=-Xmx300m

ENTRYPOINT [ "java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar" ]
USER 1001
