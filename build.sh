#!/usr/bin/env bash
# build.sh — Download ANTLR4, generate parser, compile, and create iflow CLI
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

ANTLR_VERSION="4.13.2"
ANTLR_JAR="lib/antlr-${ANTLR_VERSION}-complete.jar"
ANTLR_URL="https://www.antlr.org/download/antlr-${ANTLR_VERSION}-complete.jar"

# 1. Download ANTLR4 jar if not already present
mkdir -p lib
if [ ! -f "${ANTLR_JAR}" ]; then
    echo "[1/5] Downloading ANTLR ${ANTLR_VERSION}..."
    curl -sSL -o "${ANTLR_JAR}" "${ANTLR_URL}"
else
    echo "[1/5] ANTLR jar already present — skipping download."
fi

# 2. Generate lexer + parser Java sources from the grammar
echo "[2/5] Generating parser from grammar/IncidentFlow.g4..."
rm -rf gen && mkdir -p gen
# Run ANTLR from inside grammar/ so generated files land flat in gen/
(cd grammar && java -jar "${SCRIPT_DIR}/${ANTLR_JAR}" \
    -o "${SCRIPT_DIR}/gen" \
    -package incidentflow \
    -visitor \
    -listener \
    IncidentFlow.g4)

# 3. Compile all sources
echo "[3/5] Compiling..."
mkdir -p bin
javac -cp "${ANTLR_JAR}" -d bin \
    $(find gen -name '*.java') \
    $(find src -name '*.java')

# 4. Create iflow wrapper script (resolves its own directory at runtime)
echo "[4/5] Creating iflow CLI wrapper..."
cat > iflow << WRAPPER
#!/usr/bin/env bash
DIR="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")" && pwd)"
java -cp "\${DIR}/bin:\${DIR}/lib/antlr-${ANTLR_VERSION}-complete.jar" incidentflow.Main "\$@"
WRAPPER
chmod +x iflow

# 5. Smoke test — validate the sample playbook
echo "[5/5] Running check on playbooks/sample.iflow..."
./iflow check playbooks/sample.iflow

echo ""
echo "Build complete. Available commands:"
echo "  ./iflow check  playbooks/sample.iflow"
echo "  ./iflow report playbooks/sample.iflow [-o out.md]"
echo "  ./iflow tree   playbooks/sample.iflow"
echo "  ./iflow serve  [-p port] [-d playbooks/]"
