export const sanitizeBaseName = (name, stripExtension = false) => {
  const trimmed = (name ?? "").trim();
  if (!trimmed) return "playbook";

  const input = stripExtension ? trimmed.replace(/\.[^./\\]+$/, "") : trimmed;

  const replaced = input
    .replace(/[<>:"/\\|?*\x00-\x1F]/g, "_")
    .replace(/\s+/g, "_")
    .replace(/\.+$/g, "")
    .replace(/_+/g, "_");

  return replaced || "playbook";
};

export const downloadTextFile = ({ content, filename, mimeType }) => {
  const blob = new Blob([content], {
    type: mimeType ?? "text/plain;charset=utf-8",
  });

  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
};
