#!/bin/sh

# Make sure vault is started
while ! nc -z jme-process-archive-service-vault-server 8200 ; do sleep 1 ; done

set -exo pipefail

unset http_proxy
unset HTTP_PROXY
unset http_proxy
unset https_proxy

export VAULT_ADDR=http://jme-process-archive-service-vault-server:8200

vault login secret

vault audit enable file file_path=stdout
vault secrets enable -path=transit/jme transit

vault write -f transit/jme/keys/jme-process-archive-example-s3-key

# Enable approle auth method
vault auth enable -path=approle/jme approle

# Create policy 'jme-process-archive-service-policy' for the approle with path restriction
SCRIPT_DIR=`dirname $0`
vault policy write jme-process-archive-service-policy ${SCRIPT_DIR}/jme-process-archive-service-vault-pol.hcl

vault policy write jme-process-archive-inspection-service-policy ${SCRIPT_DIR}/jme-process-archive-inspection-service-vault-pol.hcl

# Create approle for jme-process-archive-service-approle, assign the jme-process-archive-service-policy
APPROLE_PATH=auth/approle/jme/role/jme-process-archive-service-approle
vault write ${APPROLE_PATH} \
   bind_secret_id=true \
   token_policies=jme-process-archive-service-policy

# Log approle
vault read ${APPROLE_PATH}

# Set fixed role-id for local tests
ROLE_ID=9999-8888-7777
vault write ${APPROLE_PATH}/role-id \
  role_name=jme-process-archive-service-approle \
  role_id="${ROLE_ID}"

# Set fixed secret-id for local tests
SECRET_ID=1234-5678-9012-3456
vault write ${APPROLE_PATH}/custom-secret-id \
  role_name=jme-process-archive-service-approle \
  secret_id="${SECRET_ID}"

# Create approle for jme-process-archive-inspection-service-approle, assign the jme-process-archive-inspection-service-policy
APPROLE_PATH=auth/approle/jme/role/jme-process-archive-inspection-service-approle
vault write ${APPROLE_PATH} \
   bind_secret_id=true \
   token_policies=jme-process-archive-inspection-service-policy

# Log approle
vault read ${APPROLE_PATH}

# Set fixed role-id for local tests
ROLE_ID=9999-8888-6666
vault write ${APPROLE_PATH}/role-id \
  role_name=jme-process-archive-inspection-service-approle \
  role_id="${ROLE_ID}"

# Set fixed secret-id for local tests
SECRET_ID=1234-5678-9012-6666
vault write ${APPROLE_PATH}/custom-secret-id \
  role_name=jme-process-archive-inspection-service-approle \
  secret_id="${SECRET_ID}"
