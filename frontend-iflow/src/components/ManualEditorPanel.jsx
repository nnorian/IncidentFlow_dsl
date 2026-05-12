import { useRef } from "react";
import ReactMarkdown from "react-markdown";

import { downloadTextFile, sanitizeBaseName } from "../utils/fileTransfer";

const ManualEditorPanel = ({
  code,
  setCode,
  result,
  report,
  errors,
  setErrors,
  loading,
  fileBaseName,
  setFileBaseName,
  buttonStyle,
  onValidate,
  onGenerateReport,
  onLoadExample,
}) => {
  const buttonStyleDisabled = {
    ...buttonStyle,
    opacity: 0.45,
    cursor: "not-allowed",
    boxShadow: "none",
  };

  const canExportSource = code.trim().length > 0;
  const canExportReport = report.trim().length > 0;

  const fileInputRef = useRef(null);

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

  return (
    <>
      {/* BUTTONS */}
      <div style={{ marginBottom: 20 }}>
        <button style={buttonStyle} onClick={onValidate}>
          Validate Code
        </button>

        <button style={buttonStyle} onClick={onGenerateReport}>
          Generate Report
        </button>

        <button style={buttonStyle} onClick={onLoadExample}>
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
        <p style={{ color: "#38bdf8", marginTop: 10 }}>⏳ Processing...</p>
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
                <ul
                  style={{
                    paddingLeft: "20px",
                    textAlign: "center",
                    listStylePosition: "inside",
                  }}
                  {...props}
                />
              ),
              ol: (props) => (
                <ol
                  style={{
                    paddingLeft: "20px",
                    textAlign: "center",
                    listStylePosition: "inside",
                  }}
                  {...props}
                />
              ),
              li: (props) => <li style={{ marginBottom: "5px" }} {...props} />,
            }}
          >
            {result}
          </ReactMarkdown>
        </div>
      )}
    </>
  );
};

export default ManualEditorPanel;
