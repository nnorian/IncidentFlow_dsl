export const buildWizardInjectionFalsePositiveSource = (severity) =>
  [
    "CONFIG team:",
    '    lead: "soc-lead"',
    '    members: ["analyst_tier2", "forensics"]',
    "    escalation_chain: tier2 -> incident_commander",
    "",
    "PLAYBOOK ProcessInjectionResponse",
    `    TRIGGER: incident.severity >= ${severity}`,
    "",
    "    PHASE triage:",
    '        LOG "T1055 alert — initiating triage"',
    "        edrAlert = validateEDRAlert(host: incident.affected_host, alert_id: incident.alert_id)",
    "        VERIFY edrAlert",
    "        ON_FAIL:",
    '            LOG "EDR alert could not be validated — closing as false positive"',
    "            SEVERITY LOW",
    "            GOTO report_close",
    '        LOG "Alert validated — no further indicators found"',
    "",
    "    PHASE report_close:",
    '        LOG "Alert closed as false positive after validation"',
    "",
    "    REPORT:",
    '        DO closeTicket(id: incident.id, resolution: "false_positive")',
    "        SEVERITY LOW",
    '        LOG "T1055 alert closed — false positive confirmed"',
    "",
  ].join("\n");

export const buildWizardPhishingSource = ({
  severity,
  emailType,
  payloadExec,
  urlVisited,
  credHarvest,
  endpointSuspected,
  needsEscalate,
}) => {
  const hasAttachment = emailType === 1 || emailType === 3;
  const hasLink = emailType === 2 || emailType === 3;

  const effectivePayloadExec = hasAttachment ? payloadExec : false;
  const effectiveUrlVisited = hasLink ? urlVisited : false;
  const effectiveCredHarvest =
    hasLink && effectiveUrlVisited ? credHarvest : false;
  const effectiveEndpointSuspected =
    !effectivePayloadExec && effectiveUrlVisited && !effectiveCredHarvest
      ? endpointSuspected
      : false;

  const endpointRisk =
    effectivePayloadExec ||
    (effectiveUrlVisited && !effectiveCredHarvest && effectiveEndpointSuspected);

  const lines = [];

  lines.push("CONFIG team:");
  lines.push('    lead: "soc-lead"');
  lines.push('    members: ["analyst_tier1", "analyst_tier2", "forensics"]');
  lines.push("    escalation_chain: tier1 -> tier2 -> incident_commander");
  lines.push("");
  lines.push("CONFIG environment:");
  lines.push('    name: "production"');
  lines.push("    level: HIGH");
  lines.push("");
  lines.push("CONFIG notification:");
  lines.push('    channel: "slack"');
  lines.push('    fallback: "email"');
  lines.push("");

  lines.push("PLAYBOOK PhishingResponse");
  lines.push(`    TRIGGER: incident.severity >= ${severity}`);
  lines.push("");

  // triage
  lines.push("    PHASE triage:");
  lines.push('        LOG "T1566 Phishing triage initiated"');
  lines.push(
    "        emailFlagged = checkEmailAlert(source: incident.alert_source)"
  );
  lines.push("        VERIFY emailFlagged");
  lines.push("        ON_FAIL:");
  lines.push(
    '            LOG "Alert could not be confirmed — closing as false positive"'
  );
  lines.push("            SEVERITY LOW");
  lines.push("            GOTO report_close");

  if (emailType === 1) {
    lines.push("        SEVERITY HIGH");
    lines.push('        LOG "Spearphishing attachment detected (T1566.001)"');
    lines.push("        GOTO contain_attachment");
    lines.push("");
  } else if (emailType === 2) {
    lines.push("        SEVERITY HIGH");
    lines.push('        LOG "Spearphishing link detected (T1566.002)"');
    lines.push("        GOTO contain_link");
    lines.push("");
  } else {
    lines.push('        hasAttachment = analyzeEmail(field: "attachment")');
    lines.push("        IF hasAttachment == TRUE THEN:");
    lines.push("            SEVERITY HIGH");
    lines.push(
      '            LOG "Spearphishing attachment detected (T1566.001)"'
    );
    lines.push("            GOTO contain_attachment");
    lines.push("        ELSE:");
    lines.push("            SEVERITY HIGH");
    lines.push('            LOG "Spearphishing link detected (T1566.002)"');
    lines.push("            GOTO contain_link");
    lines.push("");
  }

  // contain_attachment
  if (hasAttachment) {
    lines.push("    PHASE contain_attachment:");
    lines.push('        LOG "Containing spearphishing attachment — T1566.001"');
    lines.push('        affectedUsers = queryMailLogs(filter: "same_sender_hash")');
    lines.push("        PARALLEL:");
    lines.push(
      '            DO quarantineEmail(scope: affectedUsers, reason: "malicious_attachment")'
    );
    lines.push('            DO blockSender(action: "blacklist")');
    lines.push('            DO extractIOCs(type: "attachment", output: "ioc_list")');
    if (effectivePayloadExec) {
      lines.push("        SEVERITY CRITICAL");
      lines.push(
        '        LOG "Payload executed on endpoint — promoting severity"'
      );
      lines.push("        GOTO endpoint_triage");
      lines.push("");
    } else {
      lines.push('        LOG "No execution detected — notifying users"');
      lines.push("        GOTO notify_users");
      lines.push("");
    }
  }

  // contain_link
  if (hasLink) {
    lines.push("    PHASE contain_link:");
    lines.push('        LOG "Containing spearphishing link — T1566.002"');
    lines.push("        PARALLEL:");
    lines.push(
      '            DO blockURL(source: incident.malicious_url, layer: "proxy")'
    );
    lines.push('            DO blockURL(source: incident.malicious_url, layer: "dns")');
    lines.push('            DO extractIOCs(type: "url", output: "ioc_list")');
    if (effectiveCredHarvest) {
      lines.push("        SEVERITY CRITICAL");
      lines.push(
        '        LOG "Credential harvesting confirmed — resetting credentials"'
      );
      lines.push("        GOTO credential_reset");
      lines.push("");
    } else if (effectiveUrlVisited) {
      lines.push("        SEVERITY HIGH");
      lines.push('        LOG "URL visited — checking endpoint compromise"');
      lines.push("        GOTO endpoint_triage");
      lines.push("");
    } else {
      lines.push('        LOG "URL blocked before access — notifying users"');
      lines.push("        GOTO notify_users");
      lines.push("");
    }
  }

  // endpoint_triage
  if (endpointRisk) {
    lines.push("    PHASE endpoint_triage:");
    lines.push(
      '        LOG "Endpoint compromise suspected — running forensic checks"'
    );
    lines.push("        FOR EACH host IN affectedUsers.devices:");
    lines.push(
      '            result = runEDRScan(host: host, profile: "phishing_followup")'
    );
    lines.push("            IF result.malware_found == TRUE THEN:");
    lines.push(
      '                DO isolateHost(target: host, reason: "phishing_payload_exec")'
    );
    lines.push('                LOG "Host isolated"');
    lines.push("            ELSE:");
    lines.push('                LOG "Host clean"');
    lines.push("        WAIT ack FROM forensics TIMEOUT 30min");
    lines.push("        ON_TIMEOUT:");
    lines.push("            SEVERITY CRITICAL");
    lines.push("            GOTO escalate");
    lines.push("        GOTO notify_users");
    lines.push("");
  }

  // credential_reset
  if (effectiveCredHarvest) {
    lines.push("    PHASE credential_reset:");
    lines.push('        LOG "Credential compromise — forcing password resets"');
    lines.push("        PARALLEL:");
    lines.push('            DO forcePasswordReset(scope: affectedUsers)');
    lines.push('            DO revokeActiveSessions(scope: affectedUsers, provider: "IdP")');
    lines.push('            DO enableMFAEnforcement(scope: affectedUsers)');
    lines.push('        DO createTicket(priority: HIGH, type: "credential_compromise")');
    lines.push("        GOTO notify_users");
    lines.push("");
  }

  // notify_users (always)
  lines.push("    PHASE notify_users:");
  lines.push('        LOG "Notifying affected users"');
  lines.push(
    '        DO sendUserNotification(scope: affectedUsers, template: "phishing_awareness", channel: "email")'
  );
  lines.push('        DO alertOnCall(service: "security_awareness", priority: MEDIUM)');
  lines.push("        GOTO resolve");
  lines.push("");

  // escalate (always)
  lines.push("    PHASE escalate:");
  if (needsEscalate) {
    lines.push("        PARALLEL:");
    lines.push('            DO notifyManager(level: CRITICAL)');
    lines.push(
      '            DO createTicket(priority: CRITICAL, type: "phishing_escalation")'
    );
    lines.push("        SEVERITY CRITICAL");
    lines.push('        LOG "Escalated to incident commander"');
    lines.push("");
  } else {
    lines.push('        DO notifyManager(level: HIGH)');
    lines.push("        SEVERITY HIGH");
    lines.push('        LOG "Escalation notified — awaiting guidance"');
    lines.push("");
  }

  // resolve
  lines.push("    PHASE resolve:");
  lines.push("        status = verifyEmailEnvironmentClean()");
  lines.push("        VERIFY status");
  lines.push("        ON_FAIL:");
  lines.push('            LOG "Environment not clean — re-escalating"');
  lines.push("            GOTO escalate");
  lines.push("        SEVERITY LOW");
  lines.push('        LOG "Phishing campaign contained and remediated"');
  lines.push("");

  // report_close
  lines.push("    PHASE report_close:");
  lines.push('        LOG "Incident closed as false positive after triage"');
  lines.push("");

  // REPORT
  lines.push("    REPORT:");
  lines.push(
    '        DO closeTicket(id: incident.id, resolution: "phishing_remediated")'
  );
  lines.push(
    '        DO generateReport(template: "T1566_postmortem", output: "pdf")'
  );
  lines.push('        DO updateIOCFeed(source: ioc_list)');
  lines.push("        SEVERITY LOW");
  lines.push('        LOG "T1566 Phishing playbook completed"');

  return lines.join("\n");
};

export const buildWizardPowerShellSource = ({
  severity,
  loggingEnabled,
  riskLevel,
  amsiBypass,
  lateralMove,
  credDump,
  exfil,
}) => {
  const effectiveAmsiBypass = riskLevel === 1 ? amsiBypass : false;
  const effectiveLateralMove = riskLevel <= 2 ? lateralMove : false;
  const effectiveCredDump = riskLevel <= 2 ? credDump : false;
  const effectiveExfil = riskLevel <= 2 ? exfil : false;

  const lines = [];

  lines.push("CONFIG team:");
  lines.push('    lead: "soc-lead"');
  lines.push('    members: ["analyst_tier2", "forensics", "sysadmin"]');
  lines.push("    escalation_chain: tier2 -> forensics -> incident_commander");
  lines.push("");
  lines.push("CONFIG environment:");
  lines.push('    name: "production"');
  lines.push("    level: HIGH");
  lines.push("");

  lines.push("PLAYBOOK PowerShellExecution");
  lines.push(`    TRIGGER: incident.severity >= ${severity}`);
  lines.push("");

  // detect
  lines.push("    PHASE detect:");
  lines.push(
    '        LOG "T1059.001 PowerShell anomaly detected — starting analysis"'
  );
  lines.push(
    '        psLog = collectPowerShellLogs(host: incident.affected_host, types: ["ScriptBlock", "Module", "Transcription"])'
  );

  if (!loggingEnabled) {
    lines.push("        VERIFY psLog");
    lines.push("        ON_FAIL:");
    lines.push(
      '            LOG "PowerShell logging not enabled — critical visibility gap"'
    );
    lines.push("            SEVERITY CRITICAL");
    lines.push(
      '            DO alertOnCall(service: "sysadmin", priority: CRITICAL)'
    );
    lines.push("            GOTO enable_logging");
  }

  if (riskLevel === 1) {
    lines.push("        SEVERITY CRITICAL");
    lines.push('        LOG "High-risk PowerShell session — immediate containment"');
    lines.push("        GOTO contain");
    lines.push("");
  } else if (riskLevel === 2) {
    lines.push("        SEVERITY HIGH");
    lines.push('        LOG "Medium-risk session — forensic investigation required"');
    lines.push("        GOTO investigate");
    lines.push("");
  } else {
    lines.push("        SEVERITY LOW");
    lines.push('        LOG "Low risk score — moving to monitor"');
    lines.push("        GOTO monitor");
    lines.push("");
  }

  // enable_logging
  if (!loggingEnabled) {
    lines.push("    PHASE enable_logging:");
    lines.push(
      '        LOG "Enabling PowerShell telemetry — critical gap remediation"'
    );
    lines.push("        PARALLEL:");
    lines.push(
      '            DO enableGroupPolicy(setting: "ScriptBlockLogging", scope: "domain")'
    );
    lines.push(
      '            DO enableGroupPolicy(setting: "ModuleLogging", scope: "domain")'
    );
    lines.push(
      '            DO enableGroupPolicy(setting: "Transcription", scope: "domain")'
    );
    lines.push("        WAIT ack FROM sysadmin TIMEOUT 15min");
    lines.push("        ON_TIMEOUT:");
    lines.push("            SEVERITY CRITICAL");
    lines.push("            GOTO escalate");
    lines.push('        LOG "Logging re-enabled — re-running detection"');
    lines.push("        GOTO detect");
    lines.push("");
  }

  // contain
  if (riskLevel === 1) {
    lines.push("    PHASE contain:");
    lines.push('        LOG "Containing malicious PowerShell activity"');
    lines.push(
      '        DO terminateProcess(host: incident.affected_host, process: "powershell.exe", method: "force")'
    );
    if (effectiveAmsiBypass) {
      lines.push(
        '        LOG "Defense evasion detected (T1562) — isolating host"'
      );
      lines.push(
        '        DO isolateHost(target: incident.affected_host, reason: "evasion_detected")'
      );
      lines.push("        SEVERITY CRITICAL");
    } else {
      lines.push('        LOG "No evasion detected — partial containment applied"');
    }
    if (effectiveLateralMove) {
      lines.push('        LOG "Lateral movement detected — expanding scope"');
      lines.push("        FOR EACH host IN lateralHosts:");
      lines.push(
        '            DO isolateHost(target: host, reason: "lateral_movement_ps")'
      );
    } else {
      lines.push('        LOG "No lateral movement detected"');
    }
    lines.push("        GOTO investigate");
    lines.push("");
  }

  // investigate
  if (riskLevel <= 2) {
    lines.push("    PHASE investigate:");
    lines.push('        LOG "Forensic investigation of PowerShell session"');
    lines.push("        PARALLEL:");
    lines.push(
      '            DO collectArtifacts(host: incident.affected_host, types: ["prefetch", "registry_run_keys", "scheduled_tasks"])'
    );
    lines.push(
      '            DO dumpMemory(host: incident.affected_host, reason: "ps_forensics")'
    );
    lines.push(
      '            DO extractDecodedCommands(log: psLog, method: "base64_decode")'
    );
    if (effectiveCredDump) {
      lines.push('        LOG "Credential dumping detected — chained T1003"');
      lines.push("        SEVERITY CRITICAL");
      lines.push("        GOTO credential_response");
    }
    if (effectiveExfil) {
      lines.push("        SEVERITY CRITICAL");
      lines.push("        DO blockOutbound(host: incident.affected_host)");
      lines.push('        LOG "Exfiltration attempt blocked"');
    }
    lines.push("        WAIT ack FROM forensics TIMEOUT 30min");
    lines.push("        ON_TIMEOUT:");
    lines.push("            GOTO escalate");
    lines.push("        GOTO resolve");
    lines.push("");
  }

  // credential_response
  if (effectiveCredDump) {
    lines.push("    PHASE credential_response:");
    lines.push(
      '        LOG "Responding to credential compromise via PowerShell"'
    );
    lines.push("        PARALLEL:");
    lines.push('            DO forcePasswordReset(scope: "affected_accounts")');
    lines.push('            DO revokeKerberosTickets(scope: incident.affected_host)');
    lines.push(
      '            DO revokeActiveSessions(scope: "affected_accounts", provider: "IdP")'
    );
    lines.push("        GOTO resolve");
    lines.push("");
  }

  // monitor
  if (riskLevel === 3) {
    lines.push("    PHASE monitor:");
    lines.push('        LOG "Low-risk session — adding to watchlist"');
    lines.push(
      '        DO addToWatchlist(host: incident.affected_host, rule: "ps_anomaly_follow", duration: "7d")'
    );
    lines.push("        GOTO resolve");
    lines.push("");
  }

  // escalate (always)
  lines.push("    PHASE escalate:");
  lines.push("        PARALLEL:");
  lines.push('            DO notifyManager(level: CRITICAL)');
  lines.push(
    '            DO createTicket(priority: CRITICAL, type: "ps_execution_escalation")'
  );
  lines.push("        SEVERITY CRITICAL");
  lines.push(
    '        LOG "Escalated — manual incident commander takeover required"'
  );
  lines.push("");

  // resolve (always)
  lines.push("    PHASE resolve:");
  lines.push('        DO enforceExecutionPolicy(policy: "AllSigned", scope: "domain")');
  lines.push('        DO updateSIEMRules(profile: "T1059_001_hardened")');
  lines.push("        SEVERITY LOW");
  lines.push(
    '        LOG "PowerShell incident resolved and environment hardened"'
  );
  lines.push("");

  // REPORT
  lines.push("    REPORT:");
  lines.push(
    '        DO closeTicket(id: incident.id, resolution: "ps_execution_remediated")'
  );
  lines.push(
    '        DO generateReport(template: "T1059_postmortem", output: "pdf")'
  );
  lines.push(
    '        DO exportIOCs(format: "stix2", destination: "threat_intel_platform")'
  );
  lines.push("        SEVERITY LOW");
  lines.push('        LOG "T1059.001 PowerShell playbook completed"');

  return lines.join("\n");
};

export const buildWizardProcessInjectionSource = ({
  severity,
  alertValid,
  injectionType,
  privileged,
  c2Detected,
  persistenceFound,
  lateralMove,
  sandboxNeeded,
  integrity,
}) => {
  if (!alertValid) return buildWizardInjectionFalsePositiveSource(severity);

  const tags = ["DLL", "PE_Injection", "Process_Hollowing", "APC", "Unknown"];
  const tag = tags[Math.max(0, Math.min(tags.length - 1, injectionType - 1))];

  const lines = [];

  lines.push("CONFIG team:");
  lines.push('    lead: "soc-lead"');
  lines.push('    members: ["analyst_tier2", "malware_analyst", "forensics"]');
  lines.push(
    "    escalation_chain: tier2 -> malware_analyst -> incident_commander"
  );
  lines.push("");
  lines.push("CONFIG environment:");
  lines.push('    name: "production"');
  lines.push("    level: CRITICAL");
  lines.push("");
  lines.push("CONFIG notification:");
  lines.push('    channel: "slack"');
  lines.push('    fallback: "pagerduty"');
  lines.push("");

  lines.push("PLAYBOOK ProcessInjectionResponse");
  lines.push(`    TRIGGER: incident.severity >= ${severity}`);
  lines.push("");

  // triage
  lines.push("    PHASE triage:");
  lines.push('        LOG "T1055 Process Injection alert — initiating triage"');
  lines.push(
    "        edrAlert = validateEDRAlert(host: incident.affected_host, alert_id: incident.alert_id)"
  );
  lines.push("        VERIFY edrAlert");
  lines.push("        ON_FAIL:");
  lines.push(
    '            LOG "EDR alert could not be validated — possible false positive"'
  );
  lines.push("            SEVERITY LOW");
  lines.push("            GOTO report_close");
  lines.push(
    `        injectionType = classifyInjection(host: incident.affected_host, techniques: ["${tag}"])`
  );
  lines.push(`        LOG "Injection type identified: ${tag}"`);
  lines.push("        SEVERITY CRITICAL");
  lines.push("        GOTO contain");
  lines.push("");

  // contain
  lines.push("    PHASE contain:");
  lines.push('        LOG "Containing process injection on affected host"');
  lines.push("        targetProcess = getInjectedProcess(host: incident.affected_host)");
  lines.push("        sourceProcess = getInjectorProcess(host: incident.affected_host)");
  lines.push("        PARALLEL:");
  lines.push(
    "            DO terminateProcess(host: incident.affected_host, process: targetProcess, method: \"force\")"
  );
  lines.push(
    "            DO terminateProcess(host: incident.affected_host, process: sourceProcess, method: \"force\")"
  );
  lines.push(
    '            DO isolateHost(target: incident.affected_host, reason: "process_injection_detected")'
  );
  if (privileged) {
    lines.push(
      '        LOG "Injection into privileged process — escalating to CRITICAL"'
    );
    lines.push("        SEVERITY CRITICAL");
    lines.push("        GOTO credential_risk");
    lines.push("");
  } else {
    lines.push('        LOG "Non-privileged target — continuing investigation"');
    lines.push("        GOTO investigate");
    lines.push("");
  }

  // credential_risk
  if (privileged) {
    lines.push("    PHASE credential_risk:");
    lines.push(
      '        LOG "Privileged process injection — treating as full credential compromise"'
    );
    lines.push("        PARALLEL:");
    lines.push(
      '            DO dumpMemory(host: incident.affected_host, process: "lsass.exe", reason: "forensic_only")'
    );
    lines.push('            DO forcePasswordReset(scope: "all_accounts_on_host")');
    lines.push('            DO revokeKerberosTickets(scope: incident.affected_host)');
    lines.push(
      '            DO revokeActiveSessions(scope: "all_accounts_on_host", provider: "IdP")'
    );
    lines.push(
      '        DO createTicket(priority: CRITICAL, type: "credential_compromise_via_injection")'
    );
    lines.push("        WAIT ack FROM forensics TIMEOUT 20min");
    lines.push("        ON_TIMEOUT:");
    lines.push("            GOTO escalate");
    lines.push("        GOTO investigate");
    lines.push("");
  }

  // investigate
  lines.push("    PHASE investigate:");
  lines.push('        LOG "Deep forensic investigation of injection chain"');
  lines.push("        PARALLEL:");
  lines.push(
    '            DO collectArtifacts(host: incident.affected_host, types: ["loaded_dlls", "memory_regions", "hollowed_sections"])'
  );
  lines.push(
    '            DO scanForC2(host: incident.affected_host, protocol: ["HTTP", "HTTPS", "DNS"], timeframe: "last_4hr")'
  );
  lines.push(
    '            DO checkPersistenceMechanisms(host: incident.affected_host, checks: ["registry_run_keys", "scheduled_tasks", "services"])'
  );
  if (c2Detected) {
    lines.push('        LOG "C2 communication detected — blocking and tracing"');
    lines.push("        PARALLEL:");
    lines.push("            DO blockOutbound(host: incident.affected_host)");
    lines.push(
      "            DO traceC2Infrastructure(host: incident.affected_host)"
    );
  } else {
    lines.push('        LOG "No C2 communication detected"');
  }
  if (persistenceFound) {
    lines.push('        LOG "Persistence mechanism found — remediating"');
    lines.push("        DO removePersistence(host: incident.affected_host)");
  } else {
    lines.push('        LOG "No persistence detected"');
  }
  if (lateralMove) {
    lines.push('        LOG "Lateral movement detected — expanding containment"');
    lines.push("        FOR EACH host IN lateralMoves.targets:");
    lines.push("            result = checkProcessInjection(host: host)");
    lines.push("            IF result == TRUE THEN:");
    lines.push('                DO isolateHost(target: host, reason: "lateral_injection")');
    lines.push('                LOG "Secondary host isolated"');
    lines.push("            ELSE:");
    lines.push('                LOG "Host clean"');
  } else {
    lines.push('        LOG "No lateral movement confirmed"');
  }
  lines.push(
    sandboxNeeded ? "        GOTO malware_analysis" : "        GOTO resolve"
  );
  lines.push("");

  // malware_analysis
  if (sandboxNeeded) {
    lines.push("    PHASE malware_analysis:");
    lines.push('        LOG "Submitting artifacts to malware sandbox"');
    lines.push(
      '        DO submitToSandbox(artifacts: incident.collected_artifacts, sandbox: "internal_cuckoo", profile: "full_behavioral")'
    );
    lines.push("        WAIT ack FROM malware_analyst TIMEOUT 45min");
    lines.push("        ON_TIMEOUT:");
    lines.push('            LOG "Sandbox timeout — escalating for manual review"');
    lines.push("            GOTO escalate");
    lines.push("        iocReport = getSandboxReport(incident_id: incident.id)");
    lines.push("        PARALLEL:");
    lines.push("            DO updateIOCFeed(source: iocReport)");
    lines.push("            DO updateEDRSignatures(source: iocReport)");
    lines.push('            DO updateSIEMRules(profile: "T1055_injection_derived")');
    lines.push("        GOTO resolve");
    lines.push("");
  }

  // escalate (always)
  lines.push("    PHASE escalate:");
  lines.push("        PARALLEL:");
  lines.push('            DO notifyManager(level: CRITICAL)');
  lines.push(
    '            DO createTicket(priority: CRITICAL, type: "injection_escalation")'
  );
  lines.push(
    '            DO alertOnCall(service: "incident_commander", priority: CRITICAL)'
  );
  lines.push("        SEVERITY CRITICAL");
  lines.push('        LOG "Escalated — incident commander engaged"');
  lines.push("");

  // resolve
  lines.push("    PHASE resolve:");
  if (integrity === 1) {
    lines.push('        LOG "Host integrity verified — restoring to production"');
    lines.push(
      '        DO restoreHost(target: incident.affected_host, baseline: "golden_image")'
    );
  } else if (integrity === 2) {
    lines.push('        LOG "Host integrity compromised — scheduling re-image"');
    lines.push('        DO scheduleReimage(host: incident.affected_host, priority: HIGH)');
  } else {
    lines.push('        LOG "Host integrity unknown — escalating for manual review"');
    lines.push("        GOTO escalate");
  }
  lines.push("        SEVERITY LOW");
  lines.push('        LOG "Process injection incident resolved"');
  lines.push("");

  // report_close
  lines.push("    PHASE report_close:");
  lines.push('        LOG "Alert closed as false positive after validation"');
  lines.push("");

  // REPORT
  lines.push("    REPORT:");
  lines.push(
    '        DO closeTicket(id: incident.id, resolution: "injection_remediated")'
  );
  lines.push(
    '        DO generateReport(template: "T1055_postmortem", output: "pdf")'
  );
  lines.push(
    '        DO exportIOCs(format: "stix2", destination: "threat_intel_platform")'
  );
  lines.push('        DO updateRunbook(playbook: "T1055", version: "auto_increment")');
  lines.push("        SEVERITY LOW");
  lines.push('        LOG "T1055 Process Injection playbook completed"');

  return lines.join("\n");
};
