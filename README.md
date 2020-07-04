

start concourse

```
docker-compose up -d
```

configure fly

```
fly --target local login --concourse-url http://localhost:8080 -u admin -p adminlogging in to team 'main'


```

create pipeline

```
fly --target local set-pipeline --config ci/concourse-pipeline.yml --pipeline poc-svc-virtualization
```

unpause pipeline

```
fly --target local unpause-pipeline --pipeline poc-svc-virtualization
```
