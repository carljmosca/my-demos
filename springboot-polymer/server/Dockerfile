FROM java:8

COPY target/*-spring-boot.jar /app.jar

RUN groupadd --gid 1001 sboot
RUN useradd --uid 1001 -g sboot sboot

RUN chmod g=u /etc/passwd
RUN mkdir -p /home/sboot
RUN chown -R 1001:0 /home/sboot

ENV JAVA_OPTS=-Xmx300m

CMD java $JAVA_OPTS -jar /app.jar

USER 1001
