.PHONY: up down build

up:
	docker-compose run --rm starter
	docker-compose exec kafka bash -c "kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 2 --topic worcount-input"
	docker-compose exec kafka bash -c "kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 2 --topic worcount-output"

down:
	docker-compose down -v

build:
	mvn package
