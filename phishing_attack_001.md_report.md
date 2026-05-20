# IncidentFlow Incident Report

**Generated:** 2026-05-08 15:45:51

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
    - LOG: "Spearphishing attachment detected (T1566.001)"
    - GOTO: contain_attachment

### Phase: contain_attachment

- LOG: "Containing spearphishing attachment — T1566.001"
- ASSIGN: affectedUsers = queryMailLogs(filter: "same_sender_hash")
- PARALLEL:
  - DO: quarantineEmail(scope: affectedUsers, reason: "malicious_attachment")
  - DO: blockSender(action: "blacklist")
  - DO: extractIOCs(type: "attachment", output: "ioc_list")
  - LOG: "No execution detected — notifying users"
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
4. LOW
5. LOW

### Actions Performed

- quarantineEmail(scope: affectedUsers, reason: "malicious_attachment")
- blockSender(action: "blacklist")
- extractIOCs(type: "attachment", output: "ioc_list")
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
- "Containing spearphishing attachment — T1566.001"
- "No execution detected — notifying users"
- "Notifying affected users"
- "Escalated to incident commander"
- "Environment not clean — re-escalating"
- "Phishing campaign contained and remediated"
- "Incident closed as false positive after triage"
- "T1566 Phishing playbook completed"

