const API_BASE_URL = "http://localhost:8080";

const postJson = async (path, body) => {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  return res.json();
};

export const compileSource = async (code) =>
  postJson("/api/compile", {
    source: code,
  });

export const generateReport = async (code) =>
  postJson("/api/report", {
    source: code,
  });
