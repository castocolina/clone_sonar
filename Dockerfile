
FROM sonarqube:8.3-community

ENV PLUGIN_DIR=/opt/sonarqube/extensions/plugins

RUN apk add --no-cache curl tzdata

COPY build/to_remove build/to_install build/plugin-updater.sh /

EXPOSE 9000

# Timezone
RUN unlink /etc/localtime && ln -s /usr/share/zoneinfo/America/Santiago /etc/localtime && date

# WORKDIR $SONARQUBE_HOME

RUN set -x && chmod +x /plugin-updater.sh && bash /plugin-updater.sh
