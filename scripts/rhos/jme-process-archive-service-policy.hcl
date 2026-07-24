# Read- and Update permissions for TransitEngine
path "transit/jme/*" {
  capabilities = [
     "read", "update"
  ]
}

# Read-only permission on secrets for process-archive
path "bit-jme-d/jme-process-archive-service" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on profile specific secrets for jme-process-archive-service
path "bit-jme-d/jme-process-archive-service/*" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on shared secrets of the system jeap
path "bit-jme-d/shared" {
  capabilities = [
    "read"
  ]
}

# Read-only permission on profile specific shared secrets of the system jeap
path "bit-jme-d/shared/*" {
  capabilities = [
    "read"
  ]
}
