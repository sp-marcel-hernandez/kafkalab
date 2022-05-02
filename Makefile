.PHONY: up down build

up:
	docker-compose run --rm starter
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 2 --topic wordcount-input'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic wordcount-output'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic favcolors-input'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic favcolors-output'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic favcolors-intermediate-table --config cleanup.policy=compact'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic bank-transactions'
	docker-compose exec kafka bash -c 'kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic bank-balances --config cleanup.policy=compact'

down:
	docker-compose down -v

build:
	mvn package

write-sentence:
	docker-compose exec kafka bash -c 'echo "ola ke ase funsiona o ke ase" | kafka-console-producer --broker-list kafka:9092 --topic wordcount-input'

read-wordcount:
	docker-compose exec kafka bash -c 'kafka-console-consumer --bootstrap-server kafka:9092 --topic wordcount-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer'

write-colors:
	docker-compose exec kafka bash -c 'echo "stephane,blue" | kafka-console-producer --broker-list kafka:9092 --topic favcolors-input'
	docker-compose exec kafka bash -c 'echo "john,green" | kafka-console-producer --broker-list kafka:9092 --topic favcolors-input'
	docker-compose exec kafka bash -c 'echo "stephane,red" | kafka-console-producer --broker-list kafka:9092 --topic favcolors-input'
	docker-compose exec kafka bash -c 'echo "alice,red" | kafka-console-producer --broker-list kafka:9092 --topic favcolors-input'

read-colors:
	docker-compose exec kafka bash -c 'kafka-console-consumer --bootstrap-server kafka:9092 --topic favcolors-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer'
