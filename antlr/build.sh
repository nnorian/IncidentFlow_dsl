#!/usr/bin/env bash
# build.sh — Download ANTLR4, generate parser, compile, and create iflow CLI
set -e

ANTLR_VERSION="4.13.2"
ANTLR_JAR="antlr-${ANTLR_VERSION}-complete.jar"
ANTLR_URL="https://www.antlr.org/download/${ANTLR_JAR}"

# 1. Download ANTLR4 jar if not already present
if [ ! -f "${ANTLR_JAR}" ]; then
    echo "[1/5] Downloading ANTLR ${ANTLR_VERSION}..."
    curl -sSL -o "${ANTLR_JAR}" "${ANTLR_URL}"
else
    echo "[1/5] ANTLR jar already present — skipping download."
fi

# 2. Generate lexer + parser Java sources from the grammar
echo "[2/5] Generating parser from IncidentFlow.g4..."
mkdir -p gen
java -jar "${ANTLR_JAR}" \
    -o gen \
    -package incidentflow \
    -visitor \
    -listener \
    IncidentFlow.g4

# 3. Compile all sources
echo "[3/5] Compiling..."
mkdir -p bin
javac -cp "${ANTLR_JAR}" -d bin \
    $(find gen -name '*.java') \
    $(find . -maxdepth 1 -name '*.java')

# 4. Create iflow wrapper script (resolves its own directory at runtime)
echo "[4/5] Creating iflow CLI wrapper..."
cat > iflow << 'WRAPPER'
#!/usr/bin/env bash
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
java -cp "${DIR}/bin:${DIR}/antlr-4.13.2-complete.jar" incidentflow.Main "$@"
WRAPPER
chmod +x iflow

# 5. Smoke test — validate the sample playbook
echo "[5/5] Running check on sample.iflow..."
./iflow check sample.iflow

echo ""
echo "Build complete. Available commands:"
echo "  ./iflow check  sample.iflow"
echo "  ./iflow report sample.iflow [-o report.md]"
echo "  ./iflow tree   sample.iflow"
