FROM quay.io/keycloak/keycloak:24.0.0

COPY environments/keycloak/realm.json /opt/keycloak/data/import/realm.json
COPY environments/keycloak/themes     /opt/keycloak/themes

ENV KC_DB=postgres
ENV KC_CACHE=local
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY=edge
ENV KC_LOG_LEVEL=WARN
ENV JAVA_OPTS_APPEND="-Xmx300m -Xms64m"

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--import-realm", "--optimized"]
