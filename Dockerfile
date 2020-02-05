
FROM openjdk:7

ENV SONAR_VERSION=5.1.1 \
    SONARQUBE_HOME=/opt/sonarqube \
    SONARQUBE_JDBC_USERNAME=sonar \
    SONARQUBE_JDBC_PASSWORD=sonar \
    SONARQUBE_JDBC_URL=jdbc:h2:tcp://localhost:9092/sonar

EXPOSE 9000

RUN set -x \
    && cd /opt \
    && curl -o sonarqube.zip -fSL https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-$SONAR_VERSION.zip \
    && unzip sonarqube.zip \
    && mv sonarqube-$SONAR_VERSION sonarqube \
    && rm sonarqube.zip* \
    && rm -rf $SONARQUBE_HOME/bin/* \
    && rm -rf $SONARQUBE_HOME/lib/bundled-plugins/sonar-java-plugin-3.0.jar

# Timezone
RUN unlink /etc/localtime && ln -s /usr/share/zoneinfo/America/Santiago /etc/localtime && date

WORKDIR $SONARQUBE_HOME
