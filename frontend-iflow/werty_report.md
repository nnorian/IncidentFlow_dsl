# IncidentFlow Incident Report

**Generated:** 2026-05-12 20:41:39

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
    - SEVERITY HIGH
    - LOG: "Spearphishing link detected (T1566.002)"
    - GOTO: contain_link

### Phase: contain_link

- LOG: "Containing spearphishing link — T1566.002"
- PARALLEL:
  - DO: blockURL(source: incident.malicious_url, layer: "proxy")
  - DO: blockURL(source: incident.malicious_url, layer: "dns")
  - DO: extractIOCs(type: "url", output: "ioc_list")
  - SEVERITY CRITICAL
  - LOG: "Credential harvesting confirmed — resetting credentials"
  - GOTO: credential_reset

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
3. CRITICAL
4. CRITICAL
5. LOW
6. LOW

### Actions Performed

- blockURL(source: incident.malicious_url, layer: "proxy")
- blockURL(source: incident.malicious_url, layer: "dns")
- extractIOCs(type: "url", output: "ioc_list")
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
- "Spearphishing link detected (T1566.002)"
- "Containing spearphishing link — T1566.002"
- "Credential harvesting confirmed — resetting credentials"
- "Credential compromise — forcing password resets"
- "Notifying affected users"
- "Escalated to incident commander"
- "Environment not clean — re-escalating"
- "Phishing campaign contained and remediated"
- "Incident closed as false positive after triage"
- "T1566 Phishing playbook completed"

