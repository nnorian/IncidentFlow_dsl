#!/usr/bin/env bash
# build.sh — Download ANTLR4, generate parser, compile, and run on sample.iflow
set -e

ANTLR_VERSION="4.13.2"
ANTLR_JAR="antlr-${ANTLR_VERSION}-complete.jar"
ANTLR_URL="https://www.antlr.org/download/${ANTLR_JAR}"

# 1. Download ANTLR4 jar if not already present
if [ ! -f "${ANTLR_JAR}" ]; then
    echo "[1/4] Downloading ANTLR ${ANTLR_VERSION}..."
    curl -sSL -o "${ANTLR_JAR}" "${ANTLR_URL}"
else
    echo "[1/4] ANTLR jar already present — skipping download."
fi

# 2. Generate lexer + parser Java sources from the grammar
echo "[2/4] Generating parser from IncidentFlow.g4..."
mkdir -p gen
java -jar "${ANTLR_JAR}" \
    -o gen \
    -package incidentflow \
    -visitor \
    -listener \
    IncidentFlow.g4

# 3. Compile all generated sources together with the driver
echo "[3/4] Compiling..."
mkdir -p bin
javac -cp "${ANTLR_JAR}" -d bin $(find gen -name '*.java') Main.java

# 4. Run parser and open derivation tree GUI on the sample file
echo "[4/4] Parsing sample.iflow and opening derivation tree GUI..."
java -cp "bin:${ANTLR_JAR}" incidentflow.Main sample.iflow
