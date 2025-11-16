#!/usr/bin/env bash
set -e

docker exec postgres psql -U osandman -d rzd_db -c "SELECT * FROM rzd_monitoring.users order by updated_at desc;"

sudo docker exec postgres psql -U osandman -d rzd_db -c "SELECT t.task_id, t.username as user, left(t.from_station, 15) as from, \
left(t.to_station, 15) as to, t.departure_date as date, ARRAY_TO_STRING(ARRAY(SELECT jsonb_object_keys(train_departure_map)), ', ') as trains, \
t.seat_filters as filters, t.state, t.execution_count as exec, to_char(t.closed_at, 'DD.MM.YY HH24:MI:SS') as closed \
FROM rzd_monitoring.tasks t order by t.created_at desc LIMIT 30;"
