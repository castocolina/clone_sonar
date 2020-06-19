
FROM sonarqube:8.3-community

ENV PLUGIN_DIR=/opt/sonarqube/extensions/plugins

RUN apk add --no-cache curl tzdata

COPY entry/to_remove entry/to_install entry/plugin-updater.sh /

EXPOSE 9000

# RUN set -x \
#     && cd /opt \
#     && curl -o sonarqube.zip -fSL https://sonarsource.bintray.com/Distribution/sonarqube/sonarqube-$SONAR_VERSION.zip \
#     && unzip sonarqube.zip \
#     && mv sonarqube-$SONAR_VERSION sonarqube \
#     && rm sonarqube.zip* \
#     && rm -rf $SONARQUBE_HOME/bin/* \
#     && rm -rf $SONARQUBE_HOME/lib/bundled-plugins/sonar-java-plugin-3.0.jar

# Timezone
# RUN unlink /etc/localtime && ln -s /usr/share/zoneinfo/America/Santiago /etc/localtime && date

# WORKDIR $SONARQUBE_HOME

RUN set -x && chmod +x /plugin-updater.sh && bash /plugin-updater.sh
