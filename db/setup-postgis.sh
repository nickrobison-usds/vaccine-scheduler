#!/bin/sh

set -e
createuser -U "$POSTGRES_USER" -w vaccine_schedule
createdb -U "$POSTGRES_USER" -w vs -T template_postgis --maintenance-db="$POSTGRES_DB"

set -f
psql -U "$POSTGRES_USER" -d vs <<'EOSQL'
INSERT INTO tiger.loader_platform(os, declare_sect, pgbin, wget, unzip_command,
   psql, path_sep,
   loader, environ_set_command, county_process_command)
   SELECT 'geocoder',
'TMPDIR="${staging_fold}/"
UNZIPTOOL="/usr/bin/unzip"
WGETTOOL="/usr/bin/wget"
PSQL=psql
SHP2PGSQL=shp2pgsql
cd ${staging_fold}',
   pgbin, wget, unzip_command, psql, path_sep,
     loader, environ_set_command, county_process_command
   FROM tiger.loader_platform
   WHERE os = 'sh';
EOSQL

psql -U "$POSTGRES_USER" -d vs -tA -o /gisdata/nation_script_load.sh <<'EOP'
SET search_path TO tiger,public;
SELECT Loader_Generate_Nation_Script('geocoder')
EOP
cd /gisdata
chmod +x nation_script_load.sh
sh nation_script_load.sh

psql -U "$POSTGRES_USER" -d vs <<'EOS'
ALTER ROLE vaccine_schedule PASSWORD 'this_is_not_safe';
ALTER DATABASE vs set search_path TO tiger,public;
GRANT ALL PRIVILEGES ON SCHEMA tiger TO vaccine_schedule;
GRANT ALL PRIVILEGES ON SCHEMA tiger_data TO vaccine_schedule;
EOS
