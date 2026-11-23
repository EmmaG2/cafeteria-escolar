#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./mvnw -q test-compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
java --enable-native-access=ALL-UNNAMED \
  -cp "target/test-classes:target/classes:$(<target/classpath.txt)" \
  org.fernandogranados.demo.controlador.VerificacionVistas
