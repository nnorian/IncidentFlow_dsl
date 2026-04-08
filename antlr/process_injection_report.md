# IncidentFlow Incident Report

**Generated:** 2026-04-08 14:44:34

## Configuration

### team

- **lead:** "soc-lead"
- **members:** ["analyst_tier2", "malware_analyst", "forensics"]
- **escalation_chain:** tier2 -> malware_analyst -> incident_commander

### environment

- **name:** "production"
- **level:** CRITICAL

### notification

- **channel:** "slack"
- **fallback:** "pagerduty"


---

## Playbook: ProcessInjectionResponse

**Trigger:** incident.severity >= HIGH

### Phase: triage

- LOG: "T1055 Process Injection alert — initiating triage"
- ASSIGN: edrAlert = validateEDRAlert(host: incident.affected_host, alert_id: incident.alert_id)
- VERIFY: edrAlert
  - ON_FAIL:
    - LOG: "EDR alert could not be validated — possible false positive"
    - SEVERITY LOW
    - GOTO: report_close
    - ASSIGN: injectionType = classifyInjection(host: incident.affected_host, techniques: ["DLL", "PE_Injection", "Process_Hollowing", "APC"])
    - LOG: "Injection type identified"
    - SEVERITY CRITICAL
    - GOTO: contain

### Phase: contain

- LOG: "Containing process injection on affected host"
- ASSIGN: targetProcess = getInjectedProcess(host: incident.affected_host)
- ASSIGN: sourceProcess = getInjectorProcess(host: incident.affected_host)
- PARALLEL:
  - DO: terminateProcess(host: incident.affected_host, process: targetProcess, method: "force")
  - DO: terminateProcess(host: incident.affected_host, process: sourceProcess, method: "force")
  - DO: isolateHost(target: incident.affected_host, reason: "process_injection_detected")
  - ASSIGN: privilegedTarget = checkPrivilegedProcess(process: targetProcess, watchlist: ["lsass.exe", "winlogon.exe", "svchost.exe", "explorer.exe"])
  - IF privilegedTarget == TRUE:
    - THEN:
      - LOG: "Injection into privileged process — escalating to CRITICAL"
      - SEVERITY CRITICAL
      - GOTO: credential_risk
    - ELSE:
      - LOG: "Non-privileged target — continuing investigation"
      - GOTO: investigate

### Phase: credential_risk

- LOG: "Privileged process injection — treating as full credential compromise"
- PARALLEL:
  - DO: dumpMemory(host: incident.affected_host, process: "lsass.exe", reason: "forensic_only")
  - DO: forcePasswordReset(scope: "all_accounts_on_host")
  - DO: revokeKerberosTickets(scope: incident.affected_host)
  - DO: revokeActiveSessions(scope: "all_accounts_on_host", provider: "IdP")
  - DO: createTicket(priority: CRITICAL, type: "credential_compromise_via_injection")
  - WAIT ack FROM forensics TIMEOUT 20min
    - ON_TIMEOUT:
      - GOTO: escalate
      - GOTO: investigate

### Phase: investigate

- LOG: "Deep forensic investigation of injection chain"
- PARALLEL:
  - DO: collectArtifacts(host: incident.affected_host, types: ["loaded_dlls", "memory_regions", "hollowed_sections", "vad_tree"])
  - DO: scanForC2(host: incident.affected_host, protocol: ["HTTP", "HTTPS", "DNS"], timeframe: "last_4hr")
  - DO: checkPersistenceMechanisms(host: incident.affected_host, checks: ["registry_run_keys", "scheduled_tasks", "services", "startup_folder"])
  - ASSIGN: c2Connected = checkC2Beacons(host: incident.affected_host)
  - ASSIGN: persistence = checkPersistenceMechanisms(host: incident.affected_host)
  - ASSIGN: lateralMoves = queryNetworkLogs(source: incident.affected_host, event: "lateral_movement", timeframe: "last_4hr")
  - IF c2Connected == TRUE:
    - THEN:
      - LOG: "C2 communication detected — blocking and tracing"
      - PARALLEL:
        - DO: blockOutbound(host: incident.affected_host)
        - DO: traceC2Infrastructure(host: incident.affected_host)
    - ELSE:
      - LOG: "No C2 communication detected"
      - IF persistence == TRUE:
        - THEN:
          - LOG: "Persistence mechanism found — remediating"
          - DO: removePersistence(host: incident.affected_host)
        - ELSE:
          - LOG: "No persistence detected"
          - IF lateralMoves.count > 0:
            - THEN:
              - LOG: "Lateral movement detected — expanding containment"
              - FOR EACH host IN lateralMoves.targets:
                - ASSIGN: result = checkProcessInjection(host: host)
                - IF result == TRUE:
                  - THEN:
                    - DO: isolateHost(target: host, reason: "lateral_injection")
                    - LOG: "Secondary host isolated"
                  - ELSE:
                    - LOG: "No lateral movement confirmed"
                    - GOTO: malware_analysis

### Phase: malware_analysis

- LOG: "Submitting artifacts to malware sandbox"
- DO: submitToSandbox(artifacts: incident.collected_artifacts, sandbox: "internal_cuckoo", profile: "full_behavioral")
- WAIT ack FROM malware_analyst TIMEOUT 45min
  - ON_TIMEOUT:
    - LOG: "Sandbox timeout — escalating for manual review"
    - GOTO: escalate
    - ASSIGN: iocReport = getSandboxReport(incident_id: incident.id)
    - PARALLEL:
      - DO: updateIOCFeed(source: iocReport)
      - DO: updateEDRSignatures(source: iocReport)
      - DO: updateSIEMRules(profile: "T1055_injection_derived")
      - GOTO: resolve

### Phase: escalate

- PARALLEL:
  - DO: notifyManager(level: CRITICAL)
  - DO: createTicket(priority: CRITICAL, type: "injection_escalation")
  - DO: alertOnCall(service: "incident_commander", priority: CRITICAL)
  - SEVERITY CRITICAL
  - LOG: "Escalated — incident commander engaged"

### Phase: resolve

- ASSIGN: hostIntegrity = verifyHostIntegrity(host: incident.affected_host)
- IF hostIntegrity == FALSE:
  - THEN:
    - LOG: "Host integrity compromised — scheduling re-image"
    - DO: scheduleReimage(host: incident.affected_host, priority: HIGH)
  - ELSE:
    - LOG: "Host integrity verified — restoring to production"
    - DO: restoreHost(target: incident.affected_host, baseline: "golden_image")
    - SEVERITY LOW
    - LOG: "Process injection incident resolved"

### Phase: report_close

- LOG: "Alert closed as false positive after validation"

### Report

- DO: closeTicket(id: incident.id, resolution: "injection_remediated")
- DO: generateReport(template: "T1055_postmortem", output: "pdf")
- DO: exportIOCs(format: "stix2", destination: "threat_intel_platform")
- DO: updateRunbook(playbook: "T1055", version: "auto_increment")
- SEVERITY LOW
- LOG: "T1055 Process Injection playbook completed"


---

## Summary

### Severity Trail

1. LOW
2. CRITICAL
3. CRITICAL
4. CRITICAL
5. LOW
6. LOW

### Actions Performed

- terminateProcess(host: incident.affected_host, process: targetProcess, method: "force")
- terminateProcess(host: incident.affected_host, process: sourceProcess, method: "force")
- isolateHost(target: incident.affected_host, reason: "process_injection_detected")
- dumpMemory(host: incident.affected_host, process: "lsass.exe", reason: "forensic_only")
- forcePasswordReset(scope: "all_accounts_on_host")
- revokeKerberosTickets(scope: incident.affected_host)
- revokeActiveSessions(scope: "all_accounts_on_host", provider: "IdP")
- createTicket(priority: CRITICAL, type: "credential_compromise_via_injection")
- collectArtifacts(host: incident.affected_host, types: ["loaded_dlls", "memory_regions", "hollowed_sections", "vad_tree"])
- scanForC2(host: incident.affected_host, protocol: ["HTTP", "HTTPS", "DNS"], timeframe: "last_4hr")
- checkPersistenceMechanisms(host: incident.affected_host, checks: ["registry_run_keys", "scheduled_tasks", "services", "startup_folder"])
- blockOutbound(host: incident.affected_host)
- traceC2Infrastructure(host: incident.affected_host)
- removePersistence(host: incident.affected_host)
- isolateHost(target: host, reason: "lateral_injection")
- submitToSandbox(artifacts: incident.collected_artifacts, sandbox: "internal_cuckoo", profile: "full_behavioral")
- updateIOCFeed(source: iocReport)
- updateEDRSignatures(source: iocReport)
- updateSIEMRules(profile: "T1055_injection_derived")
- notifyManager(level: CRITICAL)
- createTicket(priority: CRITICAL, type: "injection_escalation")
- alertOnCall(service: "incident_commander", priority: CRITICAL)
- scheduleReimage(host: incident.affected_host, priority: HIGH)
- restoreHost(target: incident.affected_host, baseline: "golden_image")
- closeTicket(id: incident.id, resolution: "injection_remediated")
- generateReport(template: "T1055_postmortem", output: "pdf")
- exportIOCs(format: "stix2", destination: "threat_intel_platform")
- updateRunbook(playbook: "T1055", version: "auto_increment")

### Log Entries

- "T1055 Process Injection alert — initiating triage"
- "EDR alert could not be validated — possible false positive"
- "Injection type identified"
- "Containing process injection on affected host"
- "Injection into privileged process — escalating to CRITICAL"
- "Non-privileged target — continuing investigation"
- "Privileged process injection — treating as full credential compromise"
- "Deep forensic investigation of injection chain"
- "C2 communication detected — blocking and tracing"
- "No C2 communication detected"
- "Persistence mechanism found — remediating"
- "No persistence detected"
- "Lateral movement detected — expanding containment"
- "Secondary host isolated"
- "No lateral movement confirmed"
- "Submitting artifacts to malware sandbox"
- "Sandbox timeout — escalating for manual review"
- "Escalated — incident commander engaged"
- "Host integrity compromised — scheduling re-image"
- "Host integrity verified — restoring to production"
- "Process injection incident resolved"
- "Alert closed as false positive after validation"
- "T1055 Process Injection playbook completed"

