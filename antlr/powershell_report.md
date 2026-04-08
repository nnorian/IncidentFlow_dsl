# IncidentFlow Incident Report

**Generated:** 2026-04-08 14:44:34

## Configuration

### team

- **lead:** "soc-lead"
- **members:** ["analyst_tier2", "forensics", "sysadmin"]
- **escalation_chain:** tier2 -> forensics -> incident_commander

### environment

- **name:** "production"
- **level:** HIGH


---

## Playbook: PowerShellExecution

**Trigger:** incident.severity >= HIGH

### Phase: detect

- LOG: "T1059.001 PowerShell anomaly detected — starting analysis"
- ASSIGN: psLog = collectPowerShellLogs(host: incident.affected_host, types: ["ScriptBlock", "Module", "Transcription"])
- VERIFY: psLog
  - ON_FAIL:
    - LOG: "PowerShell logging not enabled — critical visibility gap"
    - SEVERITY CRITICAL
    - DO: alertOnCall(service: "sysadmin", priority: CRITICAL)
    - GOTO: enable_logging
    - ASSIGN: riskScore = analyzePSSession(log: psLog, indicators: ["EncodedCommand", "DownloadString", "IEX", "bypass", "hidden"])
    - IF riskScore >= 80:
      - THEN:
        - SEVERITY CRITICAL
        - LOG: "High-risk PowerShell session — immediate containment"
        - GOTO: contain
      - ELSE:
        - IF riskScore >= 40:
          - THEN:
            - SEVERITY HIGH
            - LOG: "Medium-risk session — forensic investigation required"
            - GOTO: investigate
          - ELSE:
            - LOG: "Low risk score — monitoring and closing"
            - SEVERITY LOW
            - GOTO: monitor

### Phase: enable_logging

- LOG: "Enabling PowerShell telemetry — critical gap remediation"
- PARALLEL:
  - DO: enableGroupPolicy(setting: "ScriptBlockLogging", scope: "domain")
  - DO: enableGroupPolicy(setting: "ModuleLogging", scope: "domain")
  - DO: enableGroupPolicy(setting: "Transcription", scope: "domain")
  - WAIT ack FROM sysadmin TIMEOUT 15min
    - ON_TIMEOUT:
      - SEVERITY CRITICAL
      - GOTO: escalate
      - LOG: "Logging re-enabled — re-running detection"
      - GOTO: detect

### Phase: contain

- LOG: "Containing malicious PowerShell activity"
- DO: terminateProcess(host: incident.affected_host, process: "powershell.exe", method: "force")
- ASSIGN: amsiBypass = checkAMSIStatus(host: incident.affected_host)
- ASSIGN: logsCleared = checkEventLog(host: incident.affected_host, log: "Security", event_id: 1102)
- IF amsiBypass == TRUE:
  - THEN:
    - LOG: "Defense evasion detected (T1562) — isolating host"
    - DO: isolateHost(target: incident.affected_host, reason: "evasion_detected")
    - SEVERITY CRITICAL
  - ELSE:
    - LOG: "No evasion detected — partial containment sufficient"
    - ASSIGN: lateralHosts = queryNetworkLogs(source: incident.affected_host, protocol: "WinRM", timeframe: "last_60min")
    - IF lateralHosts.count > 0:
      - THEN:
        - LOG: "Lateral movement detected — expanding scope"
        - FOR EACH host IN lateralHosts:
          - DO: isolateHost(target: host, reason: "lateral_movement_ps")
      - ELSE:
        - LOG: "No lateral movement detected"
        - GOTO: investigate

### Phase: investigate

- LOG: "Forensic investigation of PowerShell session"
- PARALLEL:
  - DO: collectArtifacts(host: incident.affected_host, types: ["prefetch", "registry_run_keys", "scheduled_tasks"])
  - DO: dumpMemory(host: incident.affected_host, reason: "ps_forensics")
  - DO: extractDecodedCommands(log: psLog, method: "base64_decode")
  - ASSIGN: credDump = checkForCredentialDump(host: incident.affected_host)
  - IF credDump == TRUE:
    - THEN:
      - LOG: "Credential dumping detected — chained T1003"
      - SEVERITY CRITICAL
      - GOTO: credential_response
    - ELSE:
      - LOG: "No credential access detected"
      - ASSIGN: exfilAttempt = checkOutboundTransfers(host: incident.affected_host, threshold: "10MB", timeframe: "last_2hr")
      - IF exfilAttempt == TRUE:
        - THEN:
          - SEVERITY CRITICAL
          - DO: blockOutbound(host: incident.affected_host)
          - LOG: "Exfiltration attempt blocked"
        - ELSE:
          - LOG: "No exfiltration detected"
          - WAIT ack FROM forensics TIMEOUT 30min
            - ON_TIMEOUT:
              - GOTO: escalate
              - GOTO: resolve

### Phase: credential_response

- LOG: "Responding to credential compromise via PowerShell"
- PARALLEL:
  - DO: forcePasswordReset(scope: "affected_accounts")
  - DO: revokeKerberosTickets(scope: incident.affected_host)
  - DO: revokeActiveSessions(scope: "affected_accounts", provider: "IdP")
  - GOTO: resolve

### Phase: monitor

- LOG: "Low-risk session — adding to watchlist"
- DO: addToWatchlist(host: incident.affected_host, rule: "ps_anomaly_follow", duration: "7d")

### Phase: escalate

- PARALLEL:
  - DO: notifyManager(level: CRITICAL)
  - DO: createTicket(priority: CRITICAL, type: "ps_execution_escalation")
  - SEVERITY CRITICAL
  - LOG: "Escalated — manual incident commander takeover required"

### Phase: resolve

- DO: enforceExecutionPolicy(policy: "AllSigned", scope: "domain")
- DO: updateSIEMRules(profile: "T1059_001_hardened")
- SEVERITY LOW
- LOG: "PowerShell incident resolved and environment hardened"

### Report

- DO: closeTicket(id: incident.id, resolution: "ps_execution_remediated")
- DO: generateReport(template: "T1059_postmortem", output: "pdf")
- DO: exportIOCs(format: "stix2", destination: "threat_intel_platform")
- SEVERITY LOW
- LOG: "T1059.001 PowerShell playbook completed"


---

## Summary

### Severity Trail

1. CRITICAL
2. CRITICAL
3. HIGH
4. LOW
5. CRITICAL
6. CRITICAL
7. CRITICAL
8. CRITICAL
9. CRITICAL
10. LOW
11. LOW

### Actions Performed

- alertOnCall(service: "sysadmin", priority: CRITICAL)
- enableGroupPolicy(setting: "ScriptBlockLogging", scope: "domain")
- enableGroupPolicy(setting: "ModuleLogging", scope: "domain")
- enableGroupPolicy(setting: "Transcription", scope: "domain")
- terminateProcess(host: incident.affected_host, process: "powershell.exe", method: "force")
- isolateHost(target: incident.affected_host, reason: "evasion_detected")
- isolateHost(target: host, reason: "lateral_movement_ps")
- collectArtifacts(host: incident.affected_host, types: ["prefetch", "registry_run_keys", "scheduled_tasks"])
- dumpMemory(host: incident.affected_host, reason: "ps_forensics")
- extractDecodedCommands(log: psLog, method: "base64_decode")
- blockOutbound(host: incident.affected_host)
- forcePasswordReset(scope: "affected_accounts")
- revokeKerberosTickets(scope: incident.affected_host)
- revokeActiveSessions(scope: "affected_accounts", provider: "IdP")
- addToWatchlist(host: incident.affected_host, rule: "ps_anomaly_follow", duration: "7d")
- notifyManager(level: CRITICAL)
- createTicket(priority: CRITICAL, type: "ps_execution_escalation")
- enforceExecutionPolicy(policy: "AllSigned", scope: "domain")
- updateSIEMRules(profile: "T1059_001_hardened")
- closeTicket(id: incident.id, resolution: "ps_execution_remediated")
- generateReport(template: "T1059_postmortem", output: "pdf")
- exportIOCs(format: "stix2", destination: "threat_intel_platform")

### Log Entries

- "T1059.001 PowerShell anomaly detected — starting analysis"
- "PowerShell logging not enabled — critical visibility gap"
- "High-risk PowerShell session — immediate containment"
- "Medium-risk session — forensic investigation required"
- "Low risk score — monitoring and closing"
- "Enabling PowerShell telemetry — critical gap remediation"
- "Logging re-enabled — re-running detection"
- "Containing malicious PowerShell activity"
- "Defense evasion detected (T1562) — isolating host"
- "No evasion detected — partial containment sufficient"
- "Lateral movement detected — expanding scope"
- "No lateral movement detected"
- "Forensic investigation of PowerShell session"
- "Credential dumping detected — chained T1003"
- "No credential access detected"
- "Exfiltration attempt blocked"
- "No exfiltration detected"
- "Responding to credential compromise via PowerShell"
- "Low-risk session — adding to watchlist"
- "Escalated — manual incident commander takeover required"
- "PowerShell incident resolved and environment hardened"
- "T1059.001 PowerShell playbook completed"

