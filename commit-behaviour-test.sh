#!/usr/bin/env bash

function kafka-read() {
    kafkacat -C -b localhost:9092 -t "output" -p 0 -o -10 -e
}

function kafka-write() {
   echo "$1&$2" | kafkacat -P -b localhost:9092 -t "input" -K "&"
}

function kafka-op() {
    docker-compose -f src/test/resources/docker-compose.yml $@
}

function kill-app() {
    pkill -9 -P $1
    kill -9 $1
}

function expect-output() {
    OUTPUT=`kafka-read`
    echo "Output from kafka-read (topic is 'output'): '${OUTPUT}'"
    echo "Expect it to be '$1'"

    if [ X"${OUTPUT}" = X"$1" ]; then
        echo "🐸 Worked!"
        return 0
    else
        echo "🙈 Failed!"
        exit 1
    fi
}

function finish {
  echo "Stopping Kafka..."
  kafka-op down
}

trap finish EXIT

echo "Building the application"
sbt package

echo "Starting Kafka..."
kafka-op up -d
echo "Wait for 10 seconds until Kafka is ready..."
sleep 10

echo "Send message to input topic"
kafka-write 1 "message"

echo "Run the java app"
TIME=1 sbt run &
JAVA_APP=$!

echo "The java PID is ${JAVA_APP}"
sleep 10
kill-app ${JAVA_APP}
echo "Killed the java process ${JAVA_APP}"

expect-output ""

echo "Second run..."
TIME=2 sbt run &
JAVA_APP=$!
echo "The java PID is ${JAVA_APP}"

sleep 20
kill-app ${JAVA_APP}
echo "Killed the java process ${JAVA_APP}"

expect-output "2-message"

exit $?