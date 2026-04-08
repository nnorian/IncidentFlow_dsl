# IncidentFlow Incident Report

**Generated:** 2026-04-08 16:03:24

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

**Trigger:** incident.severity >= HIGH

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
  - SEVERITY HIGH
  - LOG: "URL visited — checking endpoint compromise"
  - GOTO: endpoint_triage

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

### Phase: notify_users

- LOG: "Notifying affected users"
- DO: sendUserNotification(scope: affectedUsers, template: "phishing_awareness", channel: "email")
- DO: alertOnCall(service: "security_awareness", priority: MEDIUM)
- GOTO: resolve

### Phase: escalate

- DO: notifyManager(level: HIGH)
- SEVERITY HIGH
- LOG: "Escalation notified — awaiting guidance"

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
5. HIGH
6. LOW
7. LOW

### Actions Performed

- blockURL(source: incident.malicious_url, layer: "proxy")
- blockURL(source: incident.malicious_url, layer: "dns")
- extractIOCs(type: "url", output: "ioc_list")
- isolateHost(target: host, reason: "phishing_payload_exec")
- sendUserNotification(scope: affectedUsers, template: "phishing_awareness", channel: "email")
- alertOnCall(service: "security_awareness", priority: MEDIUM)
- notifyManager(level: HIGH)
- closeTicket(id: incident.id, resolution: "phishing_remediated")
- generateReport(template: "T1566_postmortem", output: "pdf")
- updateIOCFeed(source: ioc_list)

### Log Entries

- "T1566 Phishing triage initiated"
- "Alert could not be confirmed — closing as false positive"
- "Spearphishing link detected (T1566.002)"
- "Containing spearphishing link — T1566.002"
- "URL visited — checking endpoint compromise"
- "Endpoint compromise suspected — running forensic checks"
- "Host isolated"
- "Host clean"
- "Notifying affected users"
- "Escalation notified — awaiting guidance"
- "Environment not clean — re-escalating"
- "Phishing campaign contained and remediated"
- "Incident closed as false positive after triage"
- "T1566 Phishing playbook completed"

