import { useRef, useState } from "react";
import ReactMarkdown from "react-markdown";

function App() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState("");
  const [report, setReport] = useState("");
  const [errors, setErrors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fileBaseName, setFileBaseName] = useState("playbook");

  const fileInputRef = useRef(null);

  const sanitizeBaseName = (name, stripExtension = false) => {
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

  const downloadTextFile = ({ content, filename, mimeType }) => {
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

  const onImportClick = () => {
    fileInputRef.current?.click();
  };

  const onImportFile = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;

    try {
      const text = await file.text();
      setCode(text);
      setFileBaseName(sanitizeBaseName(file.name, true));
    } catch {
      setErrors(["Could not read file"]);
    }
  };

  const compile = async () => {
    setLoading(true);
    setErrors([]);
    setResult("");
    setReport("");

    try {
      const res = await fetch("http://localhost:8080/api/compile", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ source: code }),
      });

      const data = await res.json();

      if (!data.valid) {
        setErrors([
          ...data.syntax_errors,
          ...data.semantic_errors,
        ]);
      } else {
        setResult("✔ Code is valid!");
      }
    } catch (err) {
      setErrors(["Server error"]);
    }

    setLoading(false);
  };

  const generateReport = async () => {
    setLoading(true);
    setErrors([]);
    setResult("");
    setReport("");

    try {
      const res = await fetch("http://localhost:8080/api/report", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ source: code }),
      });

      const data = await res.json();
      const nextReport = data.report ?? "";
      setResult(nextReport);
      setReport(nextReport);
    } catch (err) {
      setErrors(["Server error"]);
    }

    setLoading(false);
  };

  const loadExample = () => {
    setCode(`PLAYBOOK Test
  TRIGGER: incident.severity >= HIGH

  PHASE test:
    LOG "hello"

  REPORT:
    LOG "done"`);
    setFileBaseName("example");
  };

  const buttonStyle = {
    marginRight: 10,
    padding: "10px 16px",
    borderRadius: "8px",
    border: "none",
    cursor: "pointer",
    background: "linear-gradient(135deg, #38bdf8, #6366f1)",
    color: "white",
    fontWeight: "bold",
    boxShadow: "0 4px 15px rgba(56,189,248,0.3)",
    transition: "0.2s",
  };

  const buttonStyleDisabled = {
    ...buttonStyle,
    opacity: 0.45,
    cursor: "not-allowed",
    boxShadow: "none",
  };

  const canExportSource = code.trim().length > 0;
  const canExportReport = report.trim().length > 0;

  return (
    <div
      style={{
        background:
          "linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #020617 100%)",
        color: "white",
        minHeight: "100vh",
        padding: "40px",
        fontFamily: "Arial",
        display: "flex",
        justifyContent: "center",
        alignItems: "flex-start",
      }}
    >
      <div
        style={{
          width: "100%",
          maxWidth: "850px",
          background: "rgba(30, 41, 59, 0.6)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(148, 163, 184, 0.2)",
          borderRadius: "12px",
          padding: "25px",
          boxShadow: "0 10px 30px rgba(0,0,0,0.4)",
        }}
      >
{/* HEADER CENTER */}
<div style={{ textAlign: "center", marginBottom: 20 }}>
  <h1
    style={{
      fontSize: "42px",
      fontWeight: "600",
      marginBottom: "5px",
      color: "#e2e8f0",
    }}
  >
    IncidentFlow UI
  </h1>
  <p style={{ fontSize: "13px", color: "#94a3b8" }}>
    Generated automatically
  </p>
</div>

        {/* BUTTONS */}
        <div style={{ marginBottom: 20 }}>
          <button style={buttonStyle} onClick={compile}>
            Validate Code
          </button>

          <button style={buttonStyle} onClick={generateReport}>
            Generate Report
          </button>

          <button style={buttonStyle} onClick={loadExample}>
            Load Example
          </button>

          <button style={buttonStyle} onClick={onImportClick}>
            Import .iflow
          </button>

          <button
            style={canExportSource ? buttonStyle : buttonStyleDisabled}
            disabled={!canExportSource}
            onClick={() =>
              downloadTextFile({
                content: code,
                filename: `${sanitizeBaseName(fileBaseName)}.iflow`,
                mimeType: "text/plain;charset=utf-8",
              })
            }
          >
            Export Source
          </button>

          <button
            style={canExportReport ? buttonStyle : buttonStyleDisabled}
            disabled={!canExportReport}
            onClick={() =>
              downloadTextFile({
                content: report,
                filename: `${sanitizeBaseName(fileBaseName)}.md`,
                mimeType: "text/markdown;charset=utf-8",
              })
            }
          >
            Export Report
          </button>

          <input
            ref={fileInputRef}
            type="file"
            accept=".iflow,.txt"
            onChange={onImportFile}
            style={{ display: "none" }}
          />
        </div>

        {/* EDITOR */}
        <textarea
          rows={14}
          style={{
            width: "100%",
            background: "#020617",
            color: "white",
            border: "1px solid #334155",
            padding: 12,
            borderRadius: "8px",
            fontFamily: "monospace",
            fontSize: "13px",
            outline: "none",
          }}
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="Write your .iflow code here..."
        />

        {/* LOADING */}
        {loading && (
          <p style={{ color: "#38bdf8", marginTop: 10 }}>
            ⏳ Processing...
          </p>
        )}

        {/* ERRORS */}
        {errors.length > 0 && (
          <div
            style={{
              marginTop: 20,
              background: "rgba(127,29,29,0.8)",
              padding: 15,
              borderRadius: 8,
              border: "1px solid rgba(248,113,113,0.3)",
              fontSize: "13px",
            }}
          >
            <h3 style={{ fontSize: "14px" }}>Errors</h3>
            {errors.map((e, i) => (
              <p key={i}>❌ {e}</p>
            ))}
          </div>
        )}

        {/* OUTPUT REPORT */}
        {result && (
          <div
            style={{
              marginTop: 20,
              background: "rgba(15,23,42,0.8)",
              padding: 20,
              borderRadius: 8,
              border: "1px solid rgba(148,163,184,0.2)",
              fontSize: "14px",
              lineHeight: "1.6",
            }}
          >
            <ReactMarkdown
              components={{
                h1: (props) => (
                  <h1
                    style={{
                      textAlign: "center",
                      fontSize: "25px",
                    }}
                    {...props}
                  />
                ),
                h2: (props) => (
                  <h2
                    style={{
                      textAlign: "center",
                      fontSize: "15px",
                      color: "#94a3b8",
                      marginBottom: "10px",
                    }}
                    {...props}
                  />
                ),
                p: (props) => (
                  <p style={{ fontSize: "14px", textAlign: "center" }} {...props} />
                ),
                ul: (props) => (
                  <ul style={{ paddingLeft: "20px", textAlign: "center", listStylePosition: "inside" }} {...props} />
                ),
                ol: (props) => (
                  <ol style={{ paddingLeft: "20px", textAlign: "center", listStylePosition: "inside" }} {...props} />
                ),
                li: (props) => (
                  <li style={{ marginBottom: "5px" }} {...props} />
                ),
              }}
            >
              {result}
            </ReactMarkdown>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;