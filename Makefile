.PHONY: up down build

up:
	docker-compose run --rm starter
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 2 --topic wordcount-input'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic wordcount-output'

down:
	docker-compose down -v

build:
	mvn package

write-sentence:
	docker-compose exec kafka bash -c 'echo "ola ke ase funsiona o ke ase" | kafka-console-producer --broker-list kafka:9092 --topic wordcount-input'

read-wordcount:
	docker-compose exec kafka bash -c 'kafka-console-consumer --bootstrap-server kafka:9092 --topic wordcount-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer'
