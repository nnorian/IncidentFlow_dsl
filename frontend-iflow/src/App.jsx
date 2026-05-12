import { useState } from "react";

import ManualEditorPanel from "./components/ManualEditorPanel";
import ModeTabs from "./components/ModeTabs";
import ScenarioWizardPanel from "./components/ScenarioWizardPanel";
import {
  compileSource,
  generateReport as requestReport,
} from "./services/incidentflowApi";
import { useScenarioWizard } from "./wizard/useScenarioWizard";

function App() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState("");
  const [report, setReport] = useState("");
  const [errors, setErrors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fileBaseName, setFileBaseName] = useState("playbook");

  const [mode, setMode] = useState("manual");
  const wizard = useScenarioWizard();

  const compile = async () => {
    setLoading(true);
    setErrors([]);
    setResult("");
    setReport("");

    try {
      const data = await compileSource(code);

      if (!data.valid) {
        setErrors([...data.syntax_errors, ...data.semantic_errors]);
      } else {
        setResult("✔ Code is valid!");
      }
    } catch {
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
      const data = await requestReport(code);
      const nextReport = data.report ?? "";
      setResult(nextReport);
      setReport(nextReport);
    } catch {
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

  const onGenerateWizard = () => {
    const source = wizard.generateSource();
    setCode(source);
    setFileBaseName(wizard.getExportBaseName());
    setMode("manual");
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

        {/* MODE TABS */}
        <ModeTabs mode={mode} setMode={setMode} />

        {/* SCENARIO WIZARD */}
        {mode === "wizard" && (
          <ScenarioWizardPanel
            wizard={wizard}
            buttonStyle={buttonStyle}
            onGenerateWizard={onGenerateWizard}
          />
        )}

        {/* MANUAL EDITOR */}
        {mode === "manual" && (
          <ManualEditorPanel
            code={code}
            setCode={setCode}
            result={result}
            report={report}
            errors={errors}
            setErrors={setErrors}
            loading={loading}
            fileBaseName={fileBaseName}
            setFileBaseName={setFileBaseName}
            buttonStyle={buttonStyle}
            onValidate={compile}
            onGenerateReport={generateReport}
            onLoadExample={loadExample}
          />
        )}
      </div>
    </div>
  );
}

export default App;
