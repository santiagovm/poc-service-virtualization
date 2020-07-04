# PoC Service Virtualization

Proof-of-Concept using WireMock to virtualize an external API during integration tests. 

## Concourse CI Setup

start concourse
```
docker-compose up -d
```

configure fly cli
```
fly --target local login --concourse-url http://localhost:8080 -u admin -p adminlogging in to team 'main'
```

create concourse pipeline
```
fly --target local set-pipeline --config ci/concourse-pipeline.yml --pipeline poc-svc-virtualization
```

unpause pipeline
```
fly --target local unpause-pipeline --pipeline poc-svc-virtualization
```
