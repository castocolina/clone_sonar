up:
	docker-compose up

start:
	docker-compose start

stop:
	docker-compose stop

down:
	docker-compose down

build:
	docker-compose up --force-recreate --build

build_local:
	docker-compose up --force-recreate --build sonar_local
