#!/bin/sh

if [ "$1" = "migrate" ]; then

  exec java -jar -DresourcesDir=BOOT-INF/classes liquibase.jar \
      --driver=org.postgresql.Driver \
      --classpath=app.jar:postgresql-42.2.1.jar \
      --changeLogFile=BOOT-INF/classes/db/changelog.xml \
      --url="jdbc:postgresql://${API_DB_HOSTNAME}/${API_DB_NAME}" \
      --username=${API_DB_USERNAME} \
      --password=${API_DB_PASSWORD} \
      update

else

  args="java "

  [ -f /app/dd-java-agent.jar ] && args="${args} -javaagent:/app/dd-java-agent.jar"

  profile="${SPRING_PROFILES_ACTIVE}"

  affinity="${DEPLOYMENT_AFFINITY}"

  # Custom profiles configuration for deployment affinity
  [ ! -z "${DEPLOYMENT_AFFINITY}" ] && profile=${profile},${affinity}

  args="${args} -Dspring.profiles.active=${profile} -jar app.jar"
  echo "Command will be: \"${args}\""

  set -- ${args}
  exec "$@"

fi
