#!/usr/bin/env bash
# install.sh — build IncidentFlow and install iflow to ~/.local/bin
set -e

INSTALL_DIR="${HOME}/.local/bin"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "→ Building iflow..."
bash "${SCRIPT_DIR}/build.sh"

echo ""
echo "→ Installing to ${INSTALL_DIR}/iflow ..."
mkdir -p "${INSTALL_DIR}"

cat > "${INSTALL_DIR}/iflow" << EOF
#!/usr/bin/env bash
java -cp "${SCRIPT_DIR}/bin:${SCRIPT_DIR}/lib/antlr-4.13.2-complete.jar" incidentflow.Main "\$@"
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
    echo "    iflow check  playbooks/myplaybook.iflow"
    echo "    iflow report playbooks/myplaybook.iflow -o out.md"
    echo "    iflow serve  -d playbooks/"
fi
