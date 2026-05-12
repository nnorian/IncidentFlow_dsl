const ScenarioWizardPanel = ({ wizard, buttonStyle, onGenerateWizard }) => {
  const wizardPanelStyle = {
    marginBottom: 20,
    background: "rgba(15,23,42,0.8)",
    padding: 20,
    borderRadius: 8,
    border: "1px solid rgba(148,163,184,0.2)",
    fontFamily: "monospace",
    fontSize: "13px",
  };

  const wizardOptionStyle = {
    display: "flex",
    alignItems: "center",
    gap: 10,
    padding: "8px 10px",
    marginBottom: 8,
    borderRadius: 8,
    background: "rgba(2,6,23,0.8)",
    border: "1px solid rgba(51,65,85,0.7)",
    cursor: "pointer",
  };

  const wizardInputStyle = {
    width: "100%",
    background: "#020617",
    color: "white",
    border: "1px solid #334155",
    padding: 10,
    borderRadius: "8px",
    fontFamily: "monospace",
    fontSize: "13px",
    outline: "none",
  };

  const phishingEffectiveUrlVisited = wizard.phishingHasLink
    ? wizard.phishingUrlVisited
    : false;

  return (
    <div style={wizardPanelStyle}>
      <div
        style={{
          fontSize: "18px",
          fontWeight: "bold",
          color: "#e2e8f0",
          marginBottom: 6,
        }}
      >
        IncidentFlow Wizard
      </div>
      <div style={{ color: "#94a3b8", marginBottom: 18 }}>
        Answer the questions to generate a tailored playbook and report.
      </div>

      <div style={{ fontWeight: "bold", marginBottom: 8 }}>
        Select the attack technique:
      </div>
      {[
        {
          value: 1,
          label: "1) T1566     — Phishing (Initial Access)",
        },
        {
          value: 2,
          label: "2) T1059.001 — PowerShell Execution",
        },
        {
          value: 3,
          label: "3) T1055     — Process Injection",
        },
      ].map((opt) => (
        <label key={opt.value} style={wizardOptionStyle}>
          <input
            type="radio"
            name="wizard-attack"
            checked={wizard.wizardAttack === opt.value}
            onChange={() => wizard.setWizardAttack(opt.value)}
          />
          <span>{opt.label}</span>
        </label>
      ))}

      <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
        Output base name (files will be &lt;name&gt;.iflow and
        &lt;name&gt;_report.md)
      </div>
      <input
        style={wizardInputStyle}
        value={wizard.wizardBaseName}
        onChange={(e) => wizard.setWizardBaseName(e.target.value)}
        placeholder="incident"
      />

      <div style={{ marginTop: 18, marginBottom: 12 }}>
        <div
          style={{
            fontWeight: "bold",
            fontSize: "15px",
            color: "#e2e8f0",
            marginBottom: 4,
          }}
        >
          {wizard.wizardTechniqueTitle}
        </div>
        <div style={{ color: "#94a3b8" }}>
          Answer each question to configure the playbook.
        </div>
      </div>

      <div style={{ fontWeight: "bold", marginBottom: 8 }}>
        What severity level triggered this alert?
      </div>
      {["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((s, idx) => (
        <label key={s} style={wizardOptionStyle}>
          <input
            type="radio"
            name="wizard-severity"
            checked={wizard.wizardSeverity === s}
            onChange={() => wizard.setWizardSeverity(s)}
          />
          <span>{`${idx + 1}) ${s}`}</span>
        </label>
      ))}

      {/* T1566 — Phishing */}
      {wizard.wizardAttack === 1 && (
        <>
          <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
            What type of phishing was detected?
          </div>
          {[
            {
              value: 1,
              label: "1) Spearphishing attachment (T1566.001)",
            },
            {
              value: 2,
              label: "2) Spearphishing link      (T1566.002)",
            },
            {
              value: 3,
              label: "3) Unknown / investigate both",
            },
          ].map((opt) => (
            <label key={opt.value} style={wizardOptionStyle}>
              <input
                type="radio"
                name="phishing-email-type"
                checked={wizard.phishingEmailType === opt.value}
                onChange={() => wizard.setPhishingEmailType(opt.value)}
              />
              <span>{opt.label}</span>
            </label>
          ))}

          {wizard.phishingHasAttachment && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was the attachment opened or payload executed on any endpoint?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="phishing-payload-exec"
                      checked={wizard.phishingPayloadExec === opt.v}
                      onChange={() => wizard.setPhishingPayloadExec(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}
            </>
          )}

          {wizard.phishingHasLink && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was the malicious URL visited by any user?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="phishing-url-visited"
                      checked={wizard.phishingUrlVisited === opt.v}
                      onChange={() => wizard.setPhishingUrlVisited(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              {phishingEffectiveUrlVisited && (
                <>
                  <div
                    style={{
                      fontWeight: "bold",
                      marginTop: 14,
                      marginBottom: 8,
                    }}
                  >
                    Was credential harvesting detected?
                  </div>
                  {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                    (opt) => (
                      <label key={String(opt.v)} style={wizardOptionStyle}>
                        <input
                          type="radio"
                          name="phishing-cred-harvest"
                          checked={wizard.phishingCredHarvest === opt.v}
                          onChange={() => wizard.setPhishingCredHarvest(opt.v)}
                        />
                        <span>{opt.l}</span>
                      </label>
                    )
                  )}
                </>
              )}
            </>
          )}

          {wizard.showPhishingEndpointSuspected && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Are any endpoints suspected to be compromised?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="phishing-endpoint-suspected"
                      checked={wizard.phishingEndpointSuspected === opt.v}
                      onChange={() => wizard.setPhishingEndpointSuspected(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}
            </>
          )}

          <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
            Is manual escalation to an incident commander required?
          </div>
          {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map((opt) => (
            <label key={String(opt.v)} style={wizardOptionStyle}>
              <input
                type="radio"
                name="phishing-escalate"
                checked={wizard.phishingNeedsEscalate === opt.v}
                onChange={() => wizard.setPhishingNeedsEscalate(opt.v)}
              />
              <span>{opt.l}</span>
            </label>
          ))}
        </>
      )}

      {/* T1059.001 — PowerShell */}
      {wizard.wizardAttack === 2 && (
        <>
          <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
            Is PowerShell script block logging enabled on the affected host?
          </div>
          {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map((opt) => (
            <label key={String(opt.v)} style={wizardOptionStyle}>
              <input
                type="radio"
                name="ps-logging"
                checked={wizard.psLoggingEnabled === opt.v}
                onChange={() => wizard.setPsLoggingEnabled(opt.v)}
              />
              <span>{opt.l}</span>
            </label>
          ))}

          <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
            What is the assessed risk level of this PowerShell session?
          </div>
          {[
            {
              v: 1,
              l: "1) High  — obfuscation, encoded commands, download cradles, AMSI bypass",
            },
            {
              v: 2,
              l: "2) Medium — suspicious but not confirmed malicious",
            },
            {
              v: 3,
              l: "3) Low   — anomalous but likely benign",
            },
          ].map((opt) => (
            <label key={opt.v} style={wizardOptionStyle}>
              <input
                type="radio"
                name="ps-risk"
                checked={wizard.psRiskLevel === opt.v}
                onChange={() => wizard.setPsRiskLevel(opt.v)}
              />
              <span>{opt.l}</span>
            </label>
          ))}

          {wizard.psRiskLevel === 1 && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was an AMSI bypass or defense evasion technique detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="ps-amsi"
                      checked={wizard.psAmsiBypass === opt.v}
                      onChange={() => wizard.setPsAmsiBypass(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}
            </>
          )}

          {wizard.psRiskLevel <= 2 && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was lateral movement via WinRM or PS remoting detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="ps-lateral"
                      checked={wizard.psLateralMove === opt.v}
                      onChange={() => wizard.setPsLateralMove(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was credential dumping (T1003) detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="ps-creddump"
                      checked={wizard.psCredDump === opt.v}
                      onChange={() => wizard.setPsCredDump(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was outbound data exfiltration detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="ps-exfil"
                      checked={wizard.psExfil === opt.v}
                      onChange={() => wizard.setPsExfil(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}
            </>
          )}
        </>
      )}

      {/* T1055 — Process Injection */}
      {wizard.wizardAttack === 3 && (
        <>
          <div style={{ fontWeight: "bold", marginTop: 14, marginBottom: 8 }}>
            Was the EDR alert successfully validated?
          </div>
          {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map((opt) => (
            <label key={String(opt.v)} style={wizardOptionStyle}>
              <input
                type="radio"
                name="inj-alert-valid"
                checked={wizard.injAlertValid === opt.v}
                onChange={() => wizard.setInjAlertValid(opt.v)}
              />
              <span>{opt.l}</span>
            </label>
          ))}

          {wizard.injAlertValid && (
            <>
              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                What type of process injection was identified?
              </div>
              {[
                { v: 1, l: "1) DLL Injection       (T1055.001)" },
                { v: 2, l: "2) PE Injection        (T1055.002)" },
                { v: 3, l: "3) Process Hollowing   (T1055.012)" },
                { v: 4, l: "4) APC Injection" },
                { v: 5, l: "5) Unknown / Unclassified" },
              ].map((opt) => (
                <label key={opt.v} style={wizardOptionStyle}>
                  <input
                    type="radio"
                    name="inj-type"
                    checked={wizard.injInjectionType === opt.v}
                    onChange={() => wizard.setInjInjectionType(opt.v)}
                  />
                  <span>{opt.l}</span>
                </label>
              ))}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was a privileged process targeted (lsass.exe, winlogon.exe,
                svchost.exe)?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="inj-priv"
                      checked={wizard.injPrivileged === opt.v}
                      onChange={() => wizard.setInjPrivileged(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was Command & Control (C2) communication detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="inj-c2"
                      checked={wizard.injC2Detected === opt.v}
                      onChange={() => wizard.setInjC2Detected(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was a persistence mechanism found?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="inj-pers"
                      checked={wizard.injPersistenceFound === opt.v}
                      onChange={() => wizard.setInjPersistenceFound(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Was lateral movement to other hosts detected?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="inj-lateral"
                      checked={wizard.injLateralMove === opt.v}
                      onChange={() => wizard.setInjLateralMove(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                Is malware sandbox analysis required?
              </div>
              {[{ v: true, l: "1) Yes" }, { v: false, l: "2) No" }].map(
                (opt) => (
                  <label key={String(opt.v)} style={wizardOptionStyle}>
                    <input
                      type="radio"
                      name="inj-sandbox"
                      checked={wizard.injSandboxNeeded === opt.v}
                      onChange={() => wizard.setInjSandboxNeeded(opt.v)}
                    />
                    <span>{opt.l}</span>
                  </label>
                )
              )}

              <div
                style={{
                  fontWeight: "bold",
                  marginTop: 14,
                  marginBottom: 8,
                }}
              >
                What is the result of the host integrity check?
              </div>
              {[
                {
                  v: 1,
                  l: "1) Verified   — host can be restored from golden image",
                },
                {
                  v: 2,
                  l: "2) Compromised — host must be re-imaged",
                },
                {
                  v: 3,
                  l: "3) Unknown    — escalate for manual review",
                },
              ].map((opt) => (
                <label key={opt.v} style={wizardOptionStyle}>
                  <input
                    type="radio"
                    name="inj-integrity"
                    checked={wizard.injIntegrity === opt.v}
                    onChange={() => wizard.setInjIntegrity(opt.v)}
                  />
                  <span>{opt.l}</span>
                </label>
              ))}
            </>
          )}
        </>
      )}

      <div style={{ marginTop: 18 }}>
        <button style={buttonStyle} onClick={onGenerateWizard}>
          Generate and load into editor
        </button>
      </div>
    </div>
  );
};

export default ScenarioWizardPanel;
