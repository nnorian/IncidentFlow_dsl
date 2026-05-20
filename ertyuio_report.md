# IncidentFlow Incident Report

**Generated:** 2026-04-23 11:57:31

## Configuration

### team

- **lead:** "soc-lead"
- **members:** ["analyst_tier1", "analyst_tier2", "forensics"]
- **escalation_chain:** tier1 -> tier2 -> incident_commander

### environment

- **name:** "production"
- **level:** HIGH

### notification

- **channel:** "slack"
- **fallback:** "email"


---

## Playbook: PhishingResponse

**Trigger:** incident.severity >= MEDIUM

### Phase: triage

- LOG: "T1566 Phishing triage initiated"
- ASSIGN: emailFlagged = checkEmailAlert(source: incident.alert_source)
- VERIFY: emailFlagged
  - ON_FAIL:
    - LOG: "Alert could not be confirmed — closing as false positive"
    - SEVERITY LOW
    - GOTO: report_close
    - ASSIGN: hasAttachment = analyzeEmail(field: "attachment")
    - IF hasAttachment == TRUE:
      - THEN:
        - SEVERITY HIGH
        - LOG: "Spearphishing attachment detected (T1566.001)"
        - GOTO: contain_attachment
      - ELSE:
        - SEVERITY HIGH
        - LOG: "Spearphishing link detected (T1566.002)"
        - GOTO: contain_link

### Phase: contain_attachment

- LOG: "Containing spearphishing attachment — T1566.001"
- ASSIGN: affectedUsers = queryMailLogs(filter: "same_sender_hash")
- PARALLEL:
  - DO: quarantineEmail(scope: affectedUsers, reason: "malicious_attachment")
  - DO: blockSender(action: "blacklist")
  - DO: extractIOCs(type: "attachment", output: "ioc_list")
  - SEVERITY CRITICAL
  - LOG: "Payload executed on endpoint — promoting severity"
  - GOTO: endpoint_triage

### Phase: contain_link

- LOG: "Containing spearphishing link — T1566.002"
- PARALLEL:
  - DO: blockURL(source: incident.malicious_url, layer: "proxy")
  - DO: blockURL(source: incident.malicious_url, layer: "dns")
  - DO: extractIOCs(type: "url", output: "ioc_list")
  - SEVERITY CRITICAL
  - LOG: "Credential harvesting confirmed — resetting credentials"
  - GOTO: credential_reset

### Phase: endpoint_triage

- LOG: "Endpoint compromise suspected — running forensic checks"
- FOR EACH host IN affectedUsers.devices:
  - ASSIGN: result = runEDRScan(host: host, profile: "phishing_followup")
  - IF result.malware_found == TRUE:
    - THEN:
      - DO: isolateHost(target: host, reason: "phishing_payload_exec")
      - LOG: "Host isolated"
    - ELSE:
      - LOG: "Host clean"
      - WAIT ack FROM forensics TIMEOUT 30min
        - ON_TIMEOUT:
          - SEVERITY CRITICAL
          - GOTO: escalate
          - GOTO: notify_users

### Phase: credential_reset

- LOG: "Credential compromise — forcing password resets"
- PARALLEL:
  - DO: forcePasswordReset(scope: affectedUsers)
  - DO: revokeActiveSessions(scope: affectedUsers, provider: "IdP")
  - DO: enableMFAEnforcement(scope: affectedUsers)
  - DO: createTicket(priority: HIGH, type: "credential_compromise")
  - GOTO: notify_users

### Phase: notify_users

- LOG: "Notifying affected users"
- DO: sendUserNotification(scope: affectedUsers, template: "phishing_awareness", channel: "email")
- DO: alertOnCall(service: "security_awareness", priority: MEDIUM)
- GOTO: resolve

### Phase: escalate

- PARALLEL:
  - DO: notifyManager(level: CRITICAL)
  - DO: createTicket(priority: CRITICAL, type: "phishing_escalation")
  - SEVERITY CRITICAL
  - LOG: "Escalated to incident commander"

### Phase: resolve

- ASSIGN: status = verifyEmailEnvironmentClean()
- VERIFY: status
  - ON_FAIL:
    - LOG: "Environment not clean — re-escalating"
    - GOTO: escalate
    - SEVERITY LOW
    - LOG: "Phishing campaign contained and remediated"

### Phase: report_close

- LOG: "Incident closed as false positive after triage"

### Report

- DO: closeTicket(id: incident.id, resolution: "phishing_remediated")
- DO: generateReport(template: "T1566_postmortem", output: "pdf")
- DO: updateIOCFeed(source: ioc_list)
- SEVERITY LOW
- LOG: "T1566 Phishing playbook completed"


---

## Summary

### Severity Trail

1. LOW
2. HIGH
3. HIGH
4. CRITICAL
5. CRITICAL
6. CRITICAL
7. CRITICAL
8. LOW
9. LOW

### Actions Performed

- quarantineEmail(scope: affectedUsers, reason: "malicious_attachment")
- blockSender(action: "blacklist")
- extractIOCs(type: "attachment", output: "ioc_list")
- blockURL(source: incident.malicious_url, layer: "proxy")
- blockURL(source: incident.malicious_url, layer: "dns")
- extractIOCs(type: "url", output: "ioc_list")
- isolateHost(target: host, reason: "phishing_payload_exec")
- forcePasswordReset(scope: affectedUsers)
- revokeActiveSessions(scope: affectedUsers, provider: "IdP")
- enableMFAEnforcement(scope: affectedUsers)
- createTicket(priority: HIGH, type: "credential_compromise")
- sendUserNotification(scope: affectedUsers, template: "phishing_awareness", channel: "email")
- alertOnCall(service: "security_awareness", priority: MEDIUM)
- notifyManager(level: CRITICAL)
- createTicket(priority: CRITICAL, type: "phishing_escalation")
- closeTicket(id: incident.id, resolution: "phishing_remediated")
- generateReport(template: "T1566_postmortem", output: "pdf")
- updateIOCFeed(source: ioc_list)

### Log Entries

- "T1566 Phishing triage initiated"
- "Alert could not be confirmed — closing as false positive"
- "Spearphishing attachment detected (T1566.001)"
- "Spearphishing link detected (T1566.002)"
- "Containing spearphishing attachment — T1566.001"
- "Payload executed on endpoint — promoting severity"
- "Containing spearphishing link — T1566.002"
- "Credential harvesting confirmed — resetting credentials"
- "Endpoint compromise suspected — running forensic checks"
- "Host isolated"
- "Host clean"
- "Credential compromise — forcing password resets"
- "Notifying affected users"
- "Escalated to incident commander"
- "Environment not clean — re-escalating"
- "Phishing campaign contained and remediated"
- "Incident closed as false positive after triage"
- "T1566 Phishing playbook completed"

