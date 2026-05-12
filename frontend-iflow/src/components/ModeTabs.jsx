const tabStyle = (active) => ({
  padding: "8px 12px",
  borderRadius: "10px",
  border: active
    ? "1px solid rgba(56,189,248,0.55)"
    : "1px solid rgba(148, 163, 184, 0.2)",
  background: active
    ? "linear-gradient(135deg, rgba(56,189,248,0.20), rgba(99,102,241,0.20))"
    : "rgba(15,23,42,0.5)",
  color: active ? "#e2e8f0" : "#94a3b8",
  fontWeight: "bold",
  cursor: "pointer",
  fontFamily: "monospace",
});

const ModeTabs = ({ mode, setMode }) => (
  <div
    style={{
      display: "flex",
      gap: 10,
      justifyContent: "center",
      marginBottom: 20,
    }}
  >
    <button
      type="button"
      style={tabStyle(mode === "manual")}
      onClick={() => setMode("manual")}
    >
      Manual Editor
    </button>
    <button
      type="button"
      style={tabStyle(mode === "wizard")}
      onClick={() => setMode("wizard")}
    >
      Scenario Wizard
    </button>
  </div>
);

export default ModeTabs;
