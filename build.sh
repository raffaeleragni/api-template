#!/bin/bash
set -e

# NEEDED VARIABLES
# AWS_ACCESS_KEY_ID: production access key for deployment & docker push
# AWS_SECRET_ACCESS_KEY: production secret key for deployment & docker push

# OPTIONAL VARIABLES
# DOCKER_IMAGE: image name to be deployed, defaults to the repository name

SDKMAN_SDK=${SDKMAN_SDK:-"18.0.1-tem"}
DOCKER_REPO=${DOCKER_REPO:-""}
DOCKER_IMAGE=${DOCKER_IMAGE:-"$(basename `git rev-parse --show-toplevel`)"}
GIT_TAG=${GIT_TAG:=$(git tag -l --contains HEAD)}

sdkinstall() {
  if [ ! -f ~/.sdkman/bin/sdkman-init.sh ]; then rm ~/.sdkman -rf && curl -s "https://get.sdkman.io" | bash; fi
  source "$HOME/.sdkman/bin/sdkman-init.sh"
  echo sdkman_auto_answer=true > ~/.sdkman/etc/config
  echo sdkman_auto_selfupdate=true >> ~/.sdkman/etc/config
  sdkman_auto_answer=true sdkman_auto_selfupdate=true sdk install java ${SDKMAN_SDK} || true
  sdkman_auto_answer=true sdkman_auto_selfupdate=true sdk use java ${SDKMAN_SDK}
}

build_project() {
  docker-compose down
  while ! docker-compose pull; do sleep .1; done
  docker-compose up -d
  until docker-compose exec -T db psql -U postgres -c "select 1" -d postgres; do
    sleep 1
  done

  echo "=== Building with maven wrapper ==="
  ./mvnw clean install

  docker-compose down
}

build_docker() {
  echo "=== Building docker image ==="
  docker build . --build-arg APP_VERSION=${GIT_TAG:latest} -t tmpimage
}

ensure_aws_cli() {
  echo "=== Accessing ECR credentials ==="
  AWS_EXISTS="$(command -v aws >/dev/null 2>&1 && echo 1 || echo 0)"
  if [ "$AWS_EXISTS" != 1 ]; then
    pip install --user awscli
  fi
  aws ecr get-login-password --region eu-west-1 | docker login \
    --username AWS \
    --password-stdin $DOCKER_REPO
}

sonar_push() {
  ./mvnw  org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.organization=raffaeleragni -Dsonar.host.url=https://sonarcloud.io
}

docker_push() {
  if [ "$GIT_TAG" == "" ]; then
    echo "=== No tag set, not pushing any image ==="
    docker rmi "tmpimage"
    return
  fi

  ensure_aws_cli

  FULL_IMAGE="${DOCKER_REPO}/${DOCKER_IMAGE}"
  FULL_IMAGE_TAG="${FULL_IMAGE}:${GIT_TAG}"
  FULL_IMAGE_LATEST="${FULL_IMAGE}:latest"

  echo "=== Looking for image ${FULL_IMAGE_TAG} ==="
  METADATA="$( aws ecr describe-images --region eu-west-1 --repository-name=${DOCKER_IMAGE} --image-ids=imageTag=${GIT_TAG} 2> /dev/null || true)"
  EXISTS=$?
  if [ "$EXISTS" != "0" ]; then
    echo "=== Image ${FULL_IMAGE_TAG} FOUND, NOT BUILDING ==="
    docker rmi "tmpimage"
    return
  fi

  echo "=== Pushing docker image ${FULL_IMAGE_TAG} ==="
  docker tag tmpimage "${FULL_IMAGE_TAG}"
  docker tag tmpimage "${FULL_IMAGE_LATEST}"
  docker push "${FULL_IMAGE_TAG}"
  docker push "${FULL_IMAGE_LATEST}"

  docker rmi "${FULL_IMAGE_TAG}"
  docker rmi "${FULL_IMAGE_LATEST}"
  docker rmi "tmpimage"
}

sdkinstall
build_project
build_docker
sonar_push
docker_push
