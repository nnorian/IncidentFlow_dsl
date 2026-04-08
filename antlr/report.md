# IncidentFlow Incident Report

**Generated:** 2026-04-08 14:25:09

## Configuration

### team

- **lead:** "alice"
- **members:** ["bob", "carol", "dave"]
- **escalation_chain:** tier1 -> tier2 -> oncall

### environment

- **name:** "production"
- **level:** HIGH

### notification

- **channel:** "slack"
- **fallback:** "email"


---

## Imports

- "policies/escalation.iflow" as `ep`

---

## Playbook: DatabaseOutage

**Trigger:** incident.severity >= HIGH

### Phase: triage

- ASSIGN: status = pingDB()
- VERIFY: checkConnection()
  - ON_FAIL:
    - SEVERITY CRITICAL
    - LOG: "Primary DB unreachable — escalating"
    - GOTO: escalate
    - IF status == FALSE:
      - THEN:
        - DO: sendAlert(channel: "oncall", message: "DB down")
        - SEVERITY HIGH
      - ELSE:
        - LOG: "Connectivity check passed"

### Phase: investigate

- FOR EACH node IN db.nodes:
  - ASSIGN: result = healthCheck(node: node)
  - IF result == FALSE:
    - THEN:
      - LOG: "Unhealthy node detected"
      - DO: notifyPager(user: node.owner, priority: CRITICAL)
      - WAIT ack FROM oncall TIMEOUT 15min
        - ON_TIMEOUT:
          - SEVERITY CRITICAL
          - GOTO: escalate

### Phase: escalate

- PARALLEL:
  - DO: triggerPolicy(level: CRITICAL)
  - DO: takeSnapshot(scope: "db", format: "full")
  - SEVERITY CRITICAL
  - LOG: "Escalation policy triggered"

### Phase: resolve

- VERIFY: checkConnection()
  - ON_FAIL:
    - DO: executeRunbook(name: "db-failover")
    - ASSIGN: status = pingDB()
    - IF status == TRUE:
      - THEN:
        - SEVERITY LOW
        - LOG: "Database recovered"
      - ELSE:
        - LOG: "Manual intervention required"
        - GOTO: escalate

### Report

- LOG: "Incident closed"
- SEVERITY LOW
- DO: closeTicket(id: incident.id, resolution: "auto")


---

## Summary

### Severity Trail

1. CRITICAL
2. HIGH
3. CRITICAL
4. CRITICAL
5. LOW
6. LOW

### Actions Performed

- sendAlert(channel: "oncall", message: "DB down")
- notifyPager(user: node.owner, priority: CRITICAL)
- triggerPolicy(level: CRITICAL)
- takeSnapshot(scope: "db", format: "full")
- executeRunbook(name: "db-failover")
- closeTicket(id: incident.id, resolution: "auto")

### Log Entries

- "Primary DB unreachable — escalating"
- "Connectivity check passed"
- "Unhealthy node detected"
- "Escalation policy triggered"
- "Database recovered"
- "Manual intervention required"
- "Incident closed"

