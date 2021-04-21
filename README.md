# Vaccine Scheduler

This is a prototype implementation of
the [SMART Scheduling Links](https://github.com/smart-on-fhir/smart-scheduling-links/blob/master/specification.md#deep-links-hosted-by-provider-booking-portal)
API.

## Theory of operation

This repo contains two services, a `publisher` and an `api`.

The `publisher` implements the `$bulk-publish` FHIR operation and exposes some test locations, schedules and slots as
FHIR bundles.

> Note: The publisher is mostly provided for testing purposes, it's not yet ready for real usage.

The `api` regularly polls the publisher and loads the data into the repository.

Users can access the `api` and search for available slots at given locations.

## Running:

```bash
docker-compose up --build
```

### Ports:

- API: 8080
- Publisher: 9090
- Postgres: 5432
- RabbitMQ: 5672

## Run the example

By default, the application loads a handful of locations and slots, pulled from the specification repo. These locations
exist in MA for the first few days of March, 2021.

The endpoints support a couple of common search parameters.

For example, searching for locations in Boston, MA

```bash
curl --location --request GET 'http://localhost:8080/fhir/Location?address-city=Boston&address-state=MA' \
--header 'Content-Type: application/fhir+json'
```

Supported address search parameters are:

- City
- State
- Postalcode

or within a certain radius:

```bash
curl --location --request GET 'http://localhost:8080/fhir/Location?near=42.4887%5C%7C-71.2837|10|mi' \
--header 'Content-Type: application/fhir+json'
```

> Note: Supported distance units are miles (mi), kilometers (km) and meters (m).
> Searches default to 50km distance from the given point.

You can also look for slots within a period of time:

```bash
curl --location --request GET 'http://localhost:8080/fhir/Slot?&start=gt2021-03-01 \
--header 'Content-Type: application/fhir+json'
```

### Pagination

By default, the server will return results in batches of 50 but clients can request up to 500 values per page.
Pagination is supported via the `_offset` and `_count` query parameters, which correspond to the page offset, and the
number of results per page.

When returning a `Bundle` the server automatically populates the `next` and `previous` link elements, which the client
can use to request sequential pages.

### Resource IDs and timestamps

Given that the API server is an intermediary between the clients and the upstream publishers, we need to have a
consistent way of handling both resource IDs as well as update timestamps.

#### Resource IDs

When a resource is returned by the API, the `id` value corresponds to the unique identifier on the API service. In order
to enable linking back to the original source system, the original ID provided by the upstream is included as an
additional `Identifier` field. The original ID is identified by the following
system: `http://usds.gov/vaccine/source-identifier`.

#### Timestamps

Resources returned by the API contain two timestamps:

1. `lastUpdatedAt` in the `Meta` component indicates the update timestamp provided by the upstream server (if one
   exists).
1. `lastSourceSync` as an extension in the `Meta` component which indicates when the resource was updated in the API
   server (e.g. fetched from upstream). This value is always provided and is set regardless of whether or not any values
   have changed from upstream. Users can retrieve this value via
   the `http://hl7.org/fhir/StructureDefinition/lastSourceSync` system.

The means the `lastUpdatedAt` can significantly lag behind `lastSourceSync` if the upstream source refreshes their data
at a slower interval than which the API server looks for new values.

When returning a `Bundle` resource, the `lastUpdatedAt` value is set to either the maximum `lastSourceSync` time for the
bundled resources, or to the transaction timestamp.

### Refreshing the data:

The API service sets up some cron jobs to regularly poll a list of upstream publishers for their data. The cron jobs
don't actually fetch the data, instead, they submit jobs to a `RabbitMQ` queue, which is subscribed to by the various
API services. When a job is received the attached URL is polled (by calling the `$bulk-publish` endpoint) and the listed
files are downloaded and processed.

Given the hierarchical nature of the data, we can process upstreams in parallel, but we have to process the resource
groups sequentially.
(e.g. load all the locations before we load the schedules)

If you want to trigger the background refresh, there's an actuator for that:

```bash
curl http://localhost:8080/actuator/refresh
```

(wait for the job to finish)

```bash
curl http://localhost:8080/fhir/Slot
```

Tada!!!!

## Deploying

Currently, the application only supports deployment into a Heroku environment. Eventually, we'd like to be more
flexible, but so far, we haven't had the time to actually test and deploy.

### Heroku

The application features two deployable services: the `api` and the `wa-publisher`.

Each of these need to be deployed into separate Heroku apps as both need to handle HTTP traffic. The `api` application
is really too large to run in the Heroku hobby tier, so we compensate by using OpenJ9 instead of Hotpsot, which has a
smaller memory footprint.

> I know, gross

#### API Service

1. Create the application via `heroku create`
1. Rename the origin to `api` to avoid conflicts with other services:
   ```bash
   git remote rename heroku api
   ```
1. Install the required services:
   ```bash
   heroku addons:create --remote api cloudamqp:lemur
   heroku addons:create --remote api heroku-postgresql
   ```
1. Connect to the Heroku database and setup Postgis
   ```bash
   heroku pg:psql --remote api
   CREATE EXTENSION postgis;
   ```
1. Configure the Java runtime options to handle the small memory space
   ```bash
   heroku config:set --remote api JAVA_OPTS="-XX:+UseContainerSupport -Xmx300m -Xss512k -XX:CICompilerCount=2 -XX:+UseStringDeduplication -Dfile.encoding=UTF-8"
   ```
1. Add the required buildpacks in the following order:
   ```bash
   heroku buildpacks:set --remote api heroku-community/multi-procfile
   heroku buildpacks:add --remote api https://github.com/maatuss/openj9-buildpack.git
   heroku buildpacks:add --remote api heroku/gradle
   ```
   > Note: You'll need to make sure you don't have any other buildpacks enabled by default for the application.
1. Configure the `PROCFILE` variable to point to the correct location
   ```bash
   heroku config:set --remote api PROCFILE=api-application/Procfile
   ```
1. You may want to disable Liquibase migration by default, and only run it when you need to make changes
   ```bash
   heroku config:set --remote api SPRING_LIQUIBASE_ENABLED=false
   ```
1. If you're using the Mapbox Geocoder, you'll need to set the secret correctly.
   ```bash
   heroku config:set --remote api MAPBOX_TOKEN={your secret token}
   ```
1. Deploy!
   ```bash
   git push api
   ```
1. Watch it deploy
   ```bash
   heroku logs --remote api --tail
   ```

#### WA Publisher

After the nightmare that is the API application, the WA publisher is much more straightforward

1. Create the application via `heroku create`
1. Rename the origin to `wa-publish` to avoid conflicts with other services:
   ```bash
   git remote rename heroku wa-publish
   ```
1. Add the required buildpacks in the following order:
   ```bash
   heroku buildpacks:set --remote wa-publish heroku-community/multi-procfile
   heroku buildpacks:add --remote wa-publish https://github.com/maatuss/openj9-buildpack.git
   heroku buildpacks:add --remote wa-publish heroku/gradle
   ```
   > Note: You'll need to make sure you don't have any other buildpacks enabled by default for the application.
1. Configure the `PROCFILE` variable to point to the correct location
   ```bash
   heroku config:set --remote wa-publish PROCFILE=wa-publisher/Procfile
   ```
1. Deploy!
   ```bash
   git push wa-publish
   ```
1. Watch it deploy
   ```bash
   heroku logs --remote wa-publish --tail
   ```
