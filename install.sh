#!/usr/bin/env bash
# install.sh — build IncidentFlow and install iflow to ~/.local/bin
set -e

INSTALL_DIR="${HOME}/.local/bin"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANTLR_DIR="${SCRIPT_DIR}/antlr"

echo "→ Building iflow..."
cd "${ANTLR_DIR}"
bash build.sh

echo ""
echo "→ Installing to ${INSTALL_DIR}/iflow ..."
mkdir -p "${INSTALL_DIR}"

cat > "${INSTALL_DIR}/iflow" << EOF
#!/usr/bin/env bash
java -cp "${ANTLR_DIR}/bin:${ANTLR_DIR}/antlr-4.13.2-complete.jar" incidentflow.Main "\$@"
EOF
chmod +x "${INSTALL_DIR}/iflow"

echo ""
echo "✓ Done. iflow installed to ${INSTALL_DIR}/iflow"
echo ""

# Check if INSTALL_DIR is on PATH
if [[ ":${PATH}:" != *":${INSTALL_DIR}:"* ]]; then
    echo "  ⚠  ${INSTALL_DIR} is not on your PATH."
    echo "     Add this to your ~/.bashrc or ~/.zshrc:"
    echo ""
    echo "       export PATH=\"\$HOME/.local/bin:\$PATH\""
    echo ""
else
    echo "  Run it from anywhere:"
    echo ""
    echo "    iflow --help"
    echo "    iflow check  myplaybook.iflow"
    echo "    iflow report myplaybook.iflow -o report.md"
fi
