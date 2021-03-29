# Vaccine Scheduler

This is a prototype implementationo of
the [SMART Scheduling Links](https://github.com/smart-on-fhir/smart-scheduling-links/blob/master/specification.md#deep-links-hosted-by-provider-booking-portal)
API.

## Theory of operation

This repo contains two services, a `publisher` and an `api`.

The `publisher` implements the `$bulk-publish` FHIR operation and exposes some test locations, schedules and slots as
FHIR bundles.

The `api` regularly polls the publisher and loads the data into the repository.

Users can access the `api` and search for available slots at given locations.

> Note: Search features are still being implemented.

## Refreshing the data.

The API service sets up some cron jobs to regularly poll a list of upstream publishers for their data. The cron jobs
don't actually fetch the data, instead, they submit jobs to a `RabbitMQ` queue, which is subscribed to by the various
API services. When a job is received the attached URL is polled (by calling the `$bulk-publish` endpoint) and the listed
files are downloaded and processed.

Given the hierarchical nature of the data, we can process upstreams in parallel, but we have to process the resource
groups sequentially.
(e.g. load all the locations before we load the schedules)

## Running:

```
docker-compose up --build
```

### Ports:

API: 8080 Publisher: 9090 Postgres: 5432 RabbitMQ: 5672

## Run the example

```
curl http://localhost:8080/actuator/refresh
```

(wait for the job to finish)

```
curl http://localhost:8080/fhir/Slot
```

Tada!!!!
