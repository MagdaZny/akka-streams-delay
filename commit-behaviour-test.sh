#!/usr/bin/env bash

function log() {
    echo "[log] $@"
}

function kafka-read() {
    kafkacat -C -b localhost:9092 -t "output" -p 0 -o -10 -e
}

function kafka-write() {
   echo "$1" | kafkacat -P -b localhost:9092 -t "input"
}

function kafka-op() {
    docker-compose -f src/test/resources/docker-compose.yml $@
}

function kill-app() {
    pkill -9 -P $1
    kill -9 $1

    log "Killed PID $1 and child PIDs"
}

function expect-output() {
    OUTPUT=`kafka-read`
    echo "Output from kafka-read (topic is 'output'): '${OUTPUT}'"
    echo "Expect it to be '$1'"

    if [ X"${OUTPUT}" = X"$1" ]; then
        log "🐸 Worked!"
        return 0
    else
        log "🙈 Failed!"
        exit 1
    fi
}

function finish {
  log "Stopping Kafka..."
  kafka-op down
}

trap finish EXIT

log "Building the application"
sbt package

log "Starting Kafka..."
kafka-op up -d
log "Wait for 10 seconds until Kafka is ready..."
sleep 10

log "Send message to input topic"
kafka-write "message"

log "Run the java app"
TIME=1 sbt run &
JAVA_APP=$!

log "The java PID is ${JAVA_APP}"
sleep 10
kill-app ${JAVA_APP}

expect-output ""

log "Second run..."
TIME=2 sbt run &
JAVA_APP=$!
log "The java PID is ${JAVA_APP}"

sleep 20
kill-app ${JAVA_APP}

expect-output "2-message"

exit $?