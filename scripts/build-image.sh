#!/bin/bash
#
# Copyright Kroxylicious Authors.
#
# Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
#

set -eo pipefail
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
. "${SCRIPT_DIR}/common.sh"
cd "${SCRIPT_DIR}/.."
KROXYLICIOUS_VERSION=${KROXYLICIOUS_VERSION:=$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -q -DforceStdout)}
IMAGE_EXPIRY=${IMAGE_EXPIRY:-'8h'}

if [[ -z ${REGISTRY_DESTINATION:-} ]]; then
  echo "Please set REGISTRY_DESTINATION to a value like 'quay.io/<myorg>/kroxylicious', exiting"
  exit 1
fi

IMAGE="${REGISTRY_DESTINATION}:${KROXYLICIOUS_VERSION}"
declare -a LABELS=()
#TODO expand this for additional optional labels
if [ -n "${TEMP_BUILD:-}" ]; then
  LABELS+=("quay.expires-after=${IMAGE_EXPIRY}")
fi

LABEL_ARGS=
if [ ${#LABELS[@]} -gt 0 ]; then
  for lbl in "${LABELS[@]}"; do
    LABEL_ARGS+="--label ${lbl}"
  done
fi


${CONTAINER_ENGINE} build -t "${IMAGE}" ${LABEL_ARGS} --build-arg "KROXYLICIOUS_VERSION=${KROXYLICIOUS_VERSION}" \
                                        --build-arg "CURRENT_USER=${USER}" \
                                        --build-arg "CURRENT_USER_UID=$(id -u)" \
                                        .

if [[ -n ${PUSH_IMAGE:-} ]]; then
  REGISTRY_SERVER=${REGISTRY_SERVER:-$(extractRegistryServer "${REGISTRY_DESTINATION}")}
  echo "Pushing image to ${REGISTRY_SERVER}"
  ${CONTAINER_ENGINE} login ${REGISTRY_SERVER}""
  ${CONTAINER_ENGINE} push "${IMAGE}"
else
  echo "PUSH_IMAGE not set, not pushing to container registry"
fi
