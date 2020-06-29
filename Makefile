up:
	docker-compose up

start:
	docker-compose start

stop:
	docker-compose stop

destroy:
	docker-compose down

build:
	docker-compose up --force-recreate --build

build_local:
	docker-compose up --force-recreate --build sonar_local

enter:
	docker exec -it sonar_sync_sonar_local_1 /bin/bash

enterr:
	docker exec -it sonar_sync_sonar_remote_1 /bin/bash

check_java:
	bash scripts/check_java.sh

check_groovy:
	bash scripts/check_groovy.sh

clean:
	rm -rfv tmp

download: clean check_groovy
	groovy -cp scripts scripts/Download.groovy

upload: check_groovy
	groovy -cp scripts scripts/Upload.groovy
