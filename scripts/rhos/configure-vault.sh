#!/bin/sh

# Login on RHOS with token (oc login ....)

export VAULT_ADDR=https://vault-bfdcbaf9-e8b9-47c7-b296-5b0b0381aeb8.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch

# login with token
vault login [Token see Margun Keepass]

vault secrets enable -path=transit/jme transit

vault write -f transit/jme/keys/jme-process-archive-example-s3-key

# Create policy
vault policy write jme-process-archive-service-policy jme-process-archive-service-policy.hcl
vault policy write jme-process-archive-inspection-service-policy jme-process-archive-inspection-service-policy.hcl

# Create new Auth-Roles
vault write auth/jwt-p-szb-ros-shrd-npr-01/role/jme-process-archive-service-role \
   role_type="jwt" \
   bound_audiences="https://kubernetes.default.svc" \
   user_claim="/kubernetes.io/pod/name" \
   user_claim_json_pointer=true \
   bound_subject="system:serviceaccount:bit-jme-d:jme-process-archive-service-runtime-sa" \
   token_ttl="10m" \
   token_explicit_max_ttl="10m" \
   token_policies="jme-process-archive-service-policy"


vault write auth/jwt-p-szb-ros-shrd-npr-01/role/jme-process-archive-inspection-service-role \
   role_type="jwt" \
   bound_audiences="https://kubernetes.default.svc" \
   user_claim="/kubernetes.io/pod/name" \
   user_claim_json_pointer=true \
   bound_subject="system:serviceaccount:bit-jme-d:jme-process-archive-inspection-service-runtime-sa" \
   token_ttl="10m" \
   token_explicit_max_ttl="10m" \
   token_policies="jme-process-archive-inspection-service-policy"
