#!/bin/sh

set -e
createuser -U "$POSTGRES_USER" -w vaccine_schedule
createdb -U "$POSTGRES_USER" -w vs -T template_postgis --maintenance-db="$POSTGRES_DB"
createdb -U "$POSTGRES_USER" -w geocoder --maintenance-db="$POSTGRES_DB"

set -f
psql -U "$POSTGRES_USER" -d geocoder <<'EOSQL'
CREATE EXTENSION postgis;
CREATE EXTENSION fuzzystrmatch;
CREATE EXTENSION postgis_tiger_geocoder;
CREATE EXTENSION address_standardizer;
UPDATE tiger.loader_lookuptables SET load = true WHERE table_name = 'zcta510';
INSERT INTO tiger.loader_platform(os, declare_sect, pgbin, wget, unzip_command,
   psql, path_sep,
   loader, environ_set_command, county_process_command)
   SELECT 'geocoder',
'TMPDIR="${staging_fold}"
UNZIPTOOL="/usr/bin/unzip"
WGETTOOL="/usr/bin/wget"
export PGUSER="${POSTGRES_USER}"
export PGPASSWORD="${POSTGRES_PASSWORD}"
export PGDATABASE=geocoder
PSQL=psql
SHP2PGSQL=shp2pgsql
cd ${staging_fold}',
   pgbin, wget, unzip_command, psql, path_sep,
     loader, environ_set_command, county_process_command
   FROM tiger.loader_platform
   WHERE os = 'sh';
EOSQL

psql -U "$POSTGRES_USER" -d postgres <<'EOS'
ALTER DATABASE geocoder SET search_path TO tiger,public;
EOS

psql -U "$POSTGRES_USER" -d geocoder -tA -o /gisdata/nation_script_load.sh <<'EOP'
SELECT loader_generate_nation_script('geocoder')
EOP
cd /gisdata
chmod +x nation_script_load.sh
sh nation_script_load.sh

psql -U "$POSTGRES_USER" -d geocoder <<'EOS'
ALTER ROLE vaccine_schedule PASSWORD 'this_is_not_safe';
SELECT install_missing_indexes();
EOS

psql -U "$POSTGRES_USER" -d postgres <<'EOS'
GRANT ALL PRIVILEGES ON DATABASE geocoder TO vaccine_schedule;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tiger_data TO vaccine_schedule;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tiger TO vaccine_schedule;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vaccine_schedule;
EOS
