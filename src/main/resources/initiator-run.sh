#!/usr/bin/env bash
java -Djava.util.logging.SimpleFormatter.format='%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS - %4$-6s- %2$s - %5$s%6$s%n' -jar lib/task-sockets-1.0-SNAPSHOT.jar initiator 1234 localhost 10