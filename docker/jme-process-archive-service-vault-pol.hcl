# See https://www.vaultproject.io/docs/concepts/policies for details

# Read- and Update permissions for TransitEngine
path "transit/jme/*" {
  capabilities = [
     "read", "update"
  ]
}

# Read-only permission on secrets for jme-process-archive-service
path "secret/data/jme/jme-process-archive-service" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on profile specific secrets for jme-process-archive-service
path "secret/data/jme-process-archive-service/*" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on shared secrets of the system jeap
path "secret/data/jeap/shared" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on profile specific shared secrets of the system jeap
path "secret/data/jeap/shared/*" {
  capabilities = [
    "read"
  ]
}
