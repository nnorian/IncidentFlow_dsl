package incidentflow;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive wizard that guides the user through incident-specific questions
 * and generates a tailored .iflow playbook + Markdown report.
 *
 * Supported techniques:
 *   T1566     — Phishing
 *   T1059.001 — PowerShell Execution
 *   T1055     — Process Injection
 */
public class Wizard {

    private final Scanner in = new Scanner(System.in);

    // reuse colour constants from Main
    private static final String RESET  = Main.RESET;
    private static final String BOLD   = Main.BOLD;
    private static final String RED    = Main.RED;
    private static final String GREEN  = Main.GREEN;
    private static final String YELLOW = Main.YELLOW;
    private static final String CYAN   = Main.CYAN;
    private static final String DIM    = Main.DIM;

    // ================================================================== entry

    public void run() throws IOException {
        System.out.println();
        System.out.println(BOLD + "  IncidentFlow Wizard" + RESET);
        System.out.println(DIM  + "  Answer the questions to generate a tailored playbook and report." + RESET);
        System.out.println();

        int attack = menu("Select the attack technique:", new String[]{
            "T1566     — Phishing (Initial Access)",
            "T1059.001 — PowerShell Execution",
            "T1055     — Process Injection"
        });

        System.out.println();
        String base = prompt("Output base name (files will be <name>.iflow and <name>_report.md)", "incident");
        System.out.println();

        String source;
        switch (attack) {
            case 1:  source = buildPhishing();         break;
            case 2:  source = buildPowerShell();       break;
            default: source = buildProcessInjection(); break;
        }

        String iflowFile  = base + ".iflow";
        String reportFile = base + "_report.md";

        Files.write(Paths.get(iflowFile), source.getBytes());
        System.out.println(GREEN + "  ✓ " + RESET + "Playbook saved:  " + BOLD + iflowFile  + RESET);

        generateAndSaveReport(source, reportFile);
    }

    // ================================================================== T1566 — Phishing

    private String buildPhishing() {
        header("T1566 — Phishing Response");

        String  severity       = severity("What severity level triggered this alert?");

        int emailType = menu("What type of phishing was detected?", new String[]{
            "Spearphishing attachment (T1566.001)",
            "Spearphishing link      (T1566.002)",
            "Unknown / investigate both"
        });

        boolean payloadExec   = false;
        boolean urlVisited    = false;
        boolean credHarvest   = false;

        if (emailType == 1 || emailType == 3) {
            payloadExec = yesNo("Was the attachment opened or payload executed on any endpoint?");
        }
        if (emailType == 2 || emailType == 3) {
            urlVisited  = yesNo("Was the malicious URL visited by any user?");
            if (urlVisited) {
                credHarvest = yesNo("Was credential harvesting detected?");
            }
        }

        boolean endpointRisk  = payloadExec || (urlVisited && !credHarvest && yesNo("Are any endpoints suspected to be compromised?"));
        boolean needsEscalate = yesNo("Is manual escalation to an incident commander required?");

        // ---- build source ------------------------------------------------
        StringBuilder pb = new StringBuilder();

        pb.append("CONFIG team:\n")
          .append("    lead: \"soc-lead\"\n")
          .append("    members: [\"analyst_tier1\", \"analyst_tier2\", \"forensics\"]\n")
          .append("    escalation_chain: tier1 -> tier2 -> incident_commander\n\n");
        pb.append("CONFIG environment:\n")
          .append("    name: \"production\"\n")
          .append("    level: HIGH\n\n");
        pb.append("CONFIG notification:\n")
          .append("    channel: \"slack\"\n")
          .append("    fallback: \"email\"\n\n");

        pb.append("PLAYBOOK PhishingResponse\n")
          .append("    TRIGGER: incident.severity >= ").append(severity).append("\n\n");

        // triage
        pb.append("    PHASE triage:\n")
          .append("        LOG \"T1566 Phishing triage initiated\"\n")
          .append("        emailFlagged = checkEmailAlert(source: incident.alert_source)\n")
          .append("        VERIFY emailFlagged\n")
          .append("        ON_FAIL:\n")
          .append("            LOG \"Alert could not be confirmed — closing as false positive\"\n")
          .append("            SEVERITY LOW\n")
          .append("            GOTO report_close\n");

        if (emailType == 1) {
            pb.append("        SEVERITY HIGH\n")
              .append("        LOG \"Spearphishing attachment detected (T1566.001)\"\n")
              .append("        GOTO contain_attachment\n\n");
        } else if (emailType == 2) {
            pb.append("        SEVERITY HIGH\n")
              .append("        LOG \"Spearphishing link detected (T1566.002)\"\n")
              .append("        GOTO contain_link\n\n");
        } else {
            pb.append("        hasAttachment = analyzeEmail(field: \"attachment\")\n")
              .append("        IF hasAttachment == TRUE THEN:\n")
              .append("            SEVERITY HIGH\n")
              .append("            LOG \"Spearphishing attachment detected (T1566.001)\"\n")
              .append("            GOTO contain_attachment\n")
              .append("        ELSE:\n")
              .append("            SEVERITY HIGH\n")
              .append("            LOG \"Spearphishing link detected (T1566.002)\"\n")
              .append("            GOTO contain_link\n\n");
        }

        // contain_attachment
        if (emailType == 1 || emailType == 3) {
            pb.append("    PHASE contain_attachment:\n")
              .append("        LOG \"Containing spearphishing attachment — T1566.001\"\n")
              .append("        affectedUsers = queryMailLogs(filter: \"same_sender_hash\")\n")
              .append("        PARALLEL:\n")
              .append("            DO quarantineEmail(scope: affectedUsers, reason: \"malicious_attachment\")\n")
              .append("            DO blockSender(action: \"blacklist\")\n")
              .append("            DO extractIOCs(type: \"attachment\", output: \"ioc_list\")\n");
            if (payloadExec) {
                pb.append("        SEVERITY CRITICAL\n")
                  .append("        LOG \"Payload executed on endpoint — promoting severity\"\n")
                  .append("        GOTO endpoint_triage\n\n");
            } else {
                pb.append("        LOG \"No execution detected — notifying users\"\n")
                  .append("        GOTO notify_users\n\n");
            }
        }

        // contain_link
        if (emailType == 2 || emailType == 3) {
            pb.append("    PHASE contain_link:\n")
              .append("        LOG \"Containing spearphishing link — T1566.002\"\n")
              .append("        PARALLEL:\n")
              .append("            DO blockURL(source: incident.malicious_url, layer: \"proxy\")\n")
              .append("            DO blockURL(source: incident.malicious_url, layer: \"dns\")\n")
              .append("            DO extractIOCs(type: \"url\", output: \"ioc_list\")\n");
            if (credHarvest) {
                pb.append("        SEVERITY CRITICAL\n")
                  .append("        LOG \"Credential harvesting confirmed — resetting credentials\"\n")
                  .append("        GOTO credential_reset\n\n");
            } else if (urlVisited) {
                pb.append("        SEVERITY HIGH\n")
                  .append("        LOG \"URL visited — checking endpoint compromise\"\n")
                  .append("        GOTO endpoint_triage\n\n");
            } else {
                pb.append("        LOG \"URL blocked before access — notifying users\"\n")
                  .append("        GOTO notify_users\n\n");
            }
        }

        // endpoint_triage
        if (endpointRisk) {
            pb.append("    PHASE endpoint_triage:\n")
              .append("        LOG \"Endpoint compromise suspected — running forensic checks\"\n")
              .append("        FOR EACH host IN affectedUsers.devices:\n")
              .append("            result = runEDRScan(host: host, profile: \"phishing_followup\")\n")
              .append("            IF result.malware_found == TRUE THEN:\n")
              .append("                DO isolateHost(target: host, reason: \"phishing_payload_exec\")\n")
              .append("                LOG \"Host isolated\"\n")
              .append("            ELSE:\n")
              .append("                LOG \"Host clean\"\n")
              .append("        WAIT ack FROM forensics TIMEOUT 30min\n")
              .append("        ON_TIMEOUT:\n")
              .append("            SEVERITY CRITICAL\n")
              .append("            GOTO escalate\n")
              .append("        GOTO notify_users\n\n");
        }

        // credential_reset
        if (credHarvest) {
            pb.append("    PHASE credential_reset:\n")
              .append("        LOG \"Credential compromise — forcing password resets\"\n")
              .append("        PARALLEL:\n")
              .append("            DO forcePasswordReset(scope: affectedUsers)\n")
              .append("            DO revokeActiveSessions(scope: affectedUsers, provider: \"IdP\")\n")
              .append("            DO enableMFAEnforcement(scope: affectedUsers)\n")
              .append("        DO createTicket(priority: HIGH, type: \"credential_compromise\")\n")
              .append("        GOTO notify_users\n\n");
        }

        // notify_users (always)
        pb.append("    PHASE notify_users:\n")
          .append("        LOG \"Notifying affected users\"\n")
          .append("        DO sendUserNotification(scope: affectedUsers, template: \"phishing_awareness\", channel: \"email\")\n")
          .append("        DO alertOnCall(service: \"security_awareness\", priority: MEDIUM)\n")
          .append("        GOTO resolve\n\n");

        // escalate (always — needed for WAIT ON_TIMEOUT paths)
        pb.append("    PHASE escalate:\n");
        if (needsEscalate) {
            pb.append("        PARALLEL:\n")
              .append("            DO notifyManager(level: CRITICAL)\n")
              .append("            DO createTicket(priority: CRITICAL, type: \"phishing_escalation\")\n")
              .append("        SEVERITY CRITICAL\n")
              .append("        LOG \"Escalated to incident commander\"\n\n");
        } else {
            pb.append("        DO notifyManager(level: HIGH)\n")
              .append("        SEVERITY HIGH\n")
              .append("        LOG \"Escalation notified — awaiting guidance\"\n\n");
        }

        // resolve
        pb.append("    PHASE resolve:\n")
          .append("        status = verifyEmailEnvironmentClean()\n")
          .append("        VERIFY status\n")
          .append("        ON_FAIL:\n")
          .append("            LOG \"Environment not clean — re-escalating\"\n")
          .append("            GOTO escalate\n")
          .append("        SEVERITY LOW\n")
          .append("        LOG \"Phishing campaign contained and remediated\"\n\n");

        // report_close (always — needed for VERIFY ON_FAIL path in triage)
        pb.append("    PHASE report_close:\n")
          .append("        LOG \"Incident closed as false positive after triage\"\n\n");

        // REPORT
        pb.append("    REPORT:\n")
          .append("        DO closeTicket(id: incident.id, resolution: \"phishing_remediated\")\n")
          .append("        DO generateReport(template: \"T1566_postmortem\", output: \"pdf\")\n")
          .append("        DO updateIOCFeed(source: ioc_list)\n")
          .append("        SEVERITY LOW\n")
          .append("        LOG \"T1566 Phishing playbook completed\"\n");

        return pb.toString();
    }

    // ================================================================== T1059.001 — PowerShell

    private String buildPowerShell() {
        header("T1059.001 — PowerShell Execution Response");

        String  severity       = severity("What severity level triggered this alert?");
        boolean loggingEnabled = yesNo("Is PowerShell script block logging enabled on the affected host?");

        int riskLevel = menu("What is the assessed risk level of this PowerShell session?", new String[]{
            "High  — obfuscation, encoded commands, download cradles, AMSI bypass",
            "Medium — suspicious but not confirmed malicious",
            "Low   — anomalous but likely benign"
        });

        boolean amsiBypass  = (riskLevel == 1) && yesNo("Was an AMSI bypass or defense evasion technique detected?");
        boolean lateralMove = (riskLevel <= 2) && yesNo("Was lateral movement via WinRM or PS remoting detected?");
        boolean credDump    = (riskLevel <= 2) && yesNo("Was credential dumping (T1003) detected?");
        boolean exfil       = (riskLevel <= 2) && yesNo("Was outbound data exfiltration detected?");

        // ---- build source ------------------------------------------------
        StringBuilder pb = new StringBuilder();

        pb.append("CONFIG team:\n")
          .append("    lead: \"soc-lead\"\n")
          .append("    members: [\"analyst_tier2\", \"forensics\", \"sysadmin\"]\n")
          .append("    escalation_chain: tier2 -> forensics -> incident_commander\n\n");
        pb.append("CONFIG environment:\n")
          .append("    name: \"production\"\n")
          .append("    level: HIGH\n\n");

        pb.append("PLAYBOOK PowerShellExecution\n")
          .append("    TRIGGER: incident.severity >= ").append(severity).append("\n\n");

        // detect
        pb.append("    PHASE detect:\n")
          .append("        LOG \"T1059.001 PowerShell anomaly detected — starting analysis\"\n")
          .append("        psLog = collectPowerShellLogs(host: incident.affected_host, types: [\"ScriptBlock\", \"Module\", \"Transcription\"])\n");

        if (!loggingEnabled) {
            pb.append("        VERIFY psLog\n")
              .append("        ON_FAIL:\n")
              .append("            LOG \"PowerShell logging not enabled — critical visibility gap\"\n")
              .append("            SEVERITY CRITICAL\n")
              .append("            DO alertOnCall(service: \"sysadmin\", priority: CRITICAL)\n")
              .append("            GOTO enable_logging\n");
        }

        if (riskLevel == 1) {
            pb.append("        SEVERITY CRITICAL\n")
              .append("        LOG \"High-risk PowerShell session — immediate containment\"\n")
              .append("        GOTO contain\n\n");
        } else if (riskLevel == 2) {
            pb.append("        SEVERITY HIGH\n")
              .append("        LOG \"Medium-risk session — forensic investigation required\"\n")
              .append("        GOTO investigate\n\n");
        } else {
            pb.append("        SEVERITY LOW\n")
              .append("        LOG \"Low risk score — moving to monitor\"\n")
              .append("        GOTO monitor\n\n");
        }

        // enable_logging
        if (!loggingEnabled) {
            pb.append("    PHASE enable_logging:\n")
              .append("        LOG \"Enabling PowerShell telemetry — critical gap remediation\"\n")
              .append("        PARALLEL:\n")
              .append("            DO enableGroupPolicy(setting: \"ScriptBlockLogging\", scope: \"domain\")\n")
              .append("            DO enableGroupPolicy(setting: \"ModuleLogging\", scope: \"domain\")\n")
              .append("            DO enableGroupPolicy(setting: \"Transcription\", scope: \"domain\")\n")
              .append("        WAIT ack FROM sysadmin TIMEOUT 15min\n")
              .append("        ON_TIMEOUT:\n")
              .append("            SEVERITY CRITICAL\n")
              .append("            GOTO escalate\n")
              .append("        LOG \"Logging re-enabled — re-running detection\"\n")
              .append("        GOTO detect\n\n");
        }

        // contain
        if (riskLevel == 1) {
            pb.append("    PHASE contain:\n")
              .append("        LOG \"Containing malicious PowerShell activity\"\n")
              .append("        DO terminateProcess(host: incident.affected_host, process: \"powershell.exe\", method: \"force\")\n");
            if (amsiBypass) {
                pb.append("        LOG \"Defense evasion detected (T1562) — isolating host\"\n")
                  .append("        DO isolateHost(target: incident.affected_host, reason: \"evasion_detected\")\n")
                  .append("        SEVERITY CRITICAL\n");
            } else {
                pb.append("        LOG \"No evasion detected — partial containment applied\"\n");
            }
            if (lateralMove) {
                pb.append("        LOG \"Lateral movement detected — expanding scope\"\n")
                  .append("        FOR EACH host IN lateralHosts:\n")
                  .append("            DO isolateHost(target: host, reason: \"lateral_movement_ps\")\n");
            } else {
                pb.append("        LOG \"No lateral movement detected\"\n");
            }
            pb.append("        GOTO investigate\n\n");
        }

        // investigate
        if (riskLevel <= 2) {
            pb.append("    PHASE investigate:\n")
              .append("        LOG \"Forensic investigation of PowerShell session\"\n")
              .append("        PARALLEL:\n")
              .append("            DO collectArtifacts(host: incident.affected_host, types: [\"prefetch\", \"registry_run_keys\", \"scheduled_tasks\"])\n")
              .append("            DO dumpMemory(host: incident.affected_host, reason: \"ps_forensics\")\n")
              .append("            DO extractDecodedCommands(log: psLog, method: \"base64_decode\")\n");
            if (credDump) {
                pb.append("        LOG \"Credential dumping detected — chained T1003\"\n")
                  .append("        SEVERITY CRITICAL\n")
                  .append("        GOTO credential_response\n");
            }
            if (exfil) {
                pb.append("        SEVERITY CRITICAL\n")
                  .append("        DO blockOutbound(host: incident.affected_host)\n")
                  .append("        LOG \"Exfiltration attempt blocked\"\n");
            }
            pb.append("        WAIT ack FROM forensics TIMEOUT 30min\n")
              .append("        ON_TIMEOUT:\n")
              .append("            GOTO escalate\n")
              .append("        GOTO resolve\n\n");
        }

        // credential_response
        if (credDump) {
            pb.append("    PHASE credential_response:\n")
              .append("        LOG \"Responding to credential compromise via PowerShell\"\n")
              .append("        PARALLEL:\n")
              .append("            DO forcePasswordReset(scope: \"affected_accounts\")\n")
              .append("            DO revokeKerberosTickets(scope: incident.affected_host)\n")
              .append("            DO revokeActiveSessions(scope: \"affected_accounts\", provider: \"IdP\")\n")
              .append("        GOTO resolve\n\n");
        }

        // monitor
        if (riskLevel == 3) {
            pb.append("    PHASE monitor:\n")
              .append("        LOG \"Low-risk session — adding to watchlist\"\n")
              .append("        DO addToWatchlist(host: incident.affected_host, rule: \"ps_anomaly_follow\", duration: \"7d\")\n")
              .append("        GOTO resolve\n\n");
        }

        // escalate (always)
        pb.append("    PHASE escalate:\n")
          .append("        PARALLEL:\n")
          .append("            DO notifyManager(level: CRITICAL)\n")
          .append("            DO createTicket(priority: CRITICAL, type: \"ps_execution_escalation\")\n")
          .append("        SEVERITY CRITICAL\n")
          .append("        LOG \"Escalated — manual incident commander takeover required\"\n\n");

        // resolve (always)
        pb.append("    PHASE resolve:\n")
          .append("        DO enforceExecutionPolicy(policy: \"AllSigned\", scope: \"domain\")\n")
          .append("        DO updateSIEMRules(profile: \"T1059_001_hardened\")\n")
          .append("        SEVERITY LOW\n")
          .append("        LOG \"PowerShell incident resolved and environment hardened\"\n\n");

        // REPORT
        pb.append("    REPORT:\n")
          .append("        DO closeTicket(id: incident.id, resolution: \"ps_execution_remediated\")\n")
          .append("        DO generateReport(template: \"T1059_postmortem\", output: \"pdf\")\n")
          .append("        DO exportIOCs(format: \"stix2\", destination: \"threat_intel_platform\")\n")
          .append("        SEVERITY LOW\n")
          .append("        LOG \"T1059.001 PowerShell playbook completed\"\n");

        return pb.toString();
    }

    // ================================================================== T1055 — Process Injection

    private String buildProcessInjection() {
        header("T1055 — Process Injection Response");

        String  severity    = severity("What severity level triggered this alert?");
        boolean alertValid  = yesNo("Was the EDR alert successfully validated?");

        if (!alertValid) {
            return buildInjectionFalsePositive(severity);
        }

        int injectionType = menu("What type of process injection was identified?", new String[]{
            "DLL Injection       (T1055.001)",
            "PE Injection        (T1055.002)",
            "Process Hollowing   (T1055.012)",
            "APC Injection",
            "Unknown / Unclassified"
        });
        String[] tags = {"DLL", "PE_Injection", "Process_Hollowing", "APC", "Unknown"};
        String   tag  = tags[injectionType - 1];

        boolean privileged      = yesNo("Was a privileged process targeted (lsass.exe, winlogon.exe, svchost.exe)?");
        boolean c2Detected      = yesNo("Was Command & Control (C2) communication detected?");
        boolean persistenceFound= yesNo("Was a persistence mechanism found?");
        boolean lateralMove     = yesNo("Was lateral movement to other hosts detected?");
        boolean sandboxNeeded   = yesNo("Is malware sandbox analysis required?");

        int integrity = menu("What is the result of the host integrity check?", new String[]{
            "Verified   — host can be restored from golden image",
            "Compromised — host must be re-imaged",
            "Unknown    — escalate for manual review"
        });

        // ---- build source ------------------------------------------------
        StringBuilder pb = new StringBuilder();

        pb.append("CONFIG team:\n")
          .append("    lead: \"soc-lead\"\n")
          .append("    members: [\"analyst_tier2\", \"malware_analyst\", \"forensics\"]\n")
          .append("    escalation_chain: tier2 -> malware_analyst -> incident_commander\n\n");
        pb.append("CONFIG environment:\n")
          .append("    name: \"production\"\n")
          .append("    level: CRITICAL\n\n");
        pb.append("CONFIG notification:\n")
          .append("    channel: \"slack\"\n")
          .append("    fallback: \"pagerduty\"\n\n");

        pb.append("PLAYBOOK ProcessInjectionResponse\n")
          .append("    TRIGGER: incident.severity >= ").append(severity).append("\n\n");

        // triage
        pb.append("    PHASE triage:\n")
          .append("        LOG \"T1055 Process Injection alert — initiating triage\"\n")
          .append("        edrAlert = validateEDRAlert(host: incident.affected_host, alert_id: incident.alert_id)\n")
          .append("        VERIFY edrAlert\n")
          .append("        ON_FAIL:\n")
          .append("            LOG \"EDR alert could not be validated — possible false positive\"\n")
          .append("            SEVERITY LOW\n")
          .append("            GOTO report_close\n")
          .append("        injectionType = classifyInjection(host: incident.affected_host, techniques: [\"").append(tag).append("\"])\n")
          .append("        LOG \"Injection type identified: ").append(tag).append("\"\n")
          .append("        SEVERITY CRITICAL\n")
          .append("        GOTO contain\n\n");

        // contain
        pb.append("    PHASE contain:\n")
          .append("        LOG \"Containing process injection on affected host\"\n")
          .append("        targetProcess = getInjectedProcess(host: incident.affected_host)\n")
          .append("        sourceProcess = getInjectorProcess(host: incident.affected_host)\n")
          .append("        PARALLEL:\n")
          .append("            DO terminateProcess(host: incident.affected_host, process: targetProcess, method: \"force\")\n")
          .append("            DO terminateProcess(host: incident.affected_host, process: sourceProcess, method: \"force\")\n")
          .append("            DO isolateHost(target: incident.affected_host, reason: \"process_injection_detected\")\n");
        if (privileged) {
            pb.append("        LOG \"Injection into privileged process — escalating to CRITICAL\"\n")
              .append("        SEVERITY CRITICAL\n")
              .append("        GOTO credential_risk\n\n");
        } else {
            pb.append("        LOG \"Non-privileged target — continuing investigation\"\n")
              .append("        GOTO investigate\n\n");
        }

        // credential_risk
        if (privileged) {
            pb.append("    PHASE credential_risk:\n")
              .append("        LOG \"Privileged process injection — treating as full credential compromise\"\n")
              .append("        PARALLEL:\n")
              .append("            DO dumpMemory(host: incident.affected_host, process: \"lsass.exe\", reason: \"forensic_only\")\n")
              .append("            DO forcePasswordReset(scope: \"all_accounts_on_host\")\n")
              .append("            DO revokeKerberosTickets(scope: incident.affected_host)\n")
              .append("            DO revokeActiveSessions(scope: \"all_accounts_on_host\", provider: \"IdP\")\n")
              .append("        DO createTicket(priority: CRITICAL, type: \"credential_compromise_via_injection\")\n")
              .append("        WAIT ack FROM forensics TIMEOUT 20min\n")
              .append("        ON_TIMEOUT:\n")
              .append("            GOTO escalate\n")
              .append("        GOTO investigate\n\n");
        }

        // investigate
        pb.append("    PHASE investigate:\n")
          .append("        LOG \"Deep forensic investigation of injection chain\"\n")
          .append("        PARALLEL:\n")
          .append("            DO collectArtifacts(host: incident.affected_host, types: [\"loaded_dlls\", \"memory_regions\", \"hollowed_sections\"])\n")
          .append("            DO scanForC2(host: incident.affected_host, protocol: [\"HTTP\", \"HTTPS\", \"DNS\"], timeframe: \"last_4hr\")\n")
          .append("            DO checkPersistenceMechanisms(host: incident.affected_host, checks: [\"registry_run_keys\", \"scheduled_tasks\", \"services\"])\n");
        if (c2Detected) {
            pb.append("        LOG \"C2 communication detected — blocking and tracing\"\n")
              .append("        PARALLEL:\n")
              .append("            DO blockOutbound(host: incident.affected_host)\n")
              .append("            DO traceC2Infrastructure(host: incident.affected_host)\n");
        } else {
            pb.append("        LOG \"No C2 communication detected\"\n");
        }
        if (persistenceFound) {
            pb.append("        LOG \"Persistence mechanism found — remediating\"\n")
              .append("        DO removePersistence(host: incident.affected_host)\n");
        } else {
            pb.append("        LOG \"No persistence detected\"\n");
        }
        if (lateralMove) {
            pb.append("        LOG \"Lateral movement detected — expanding containment\"\n")
              .append("        FOR EACH host IN lateralMoves.targets:\n")
              .append("            result = checkProcessInjection(host: host)\n")
              .append("            IF result == TRUE THEN:\n")
              .append("                DO isolateHost(target: host, reason: \"lateral_injection\")\n")
              .append("                LOG \"Secondary host isolated\"\n")
              .append("            ELSE:\n")
              .append("                LOG \"Host clean\"\n");
        } else {
            pb.append("        LOG \"No lateral movement confirmed\"\n");
        }
        pb.append(sandboxNeeded ? "        GOTO malware_analysis\n\n" : "        GOTO resolve\n\n");

        // malware_analysis
        if (sandboxNeeded) {
            pb.append("    PHASE malware_analysis:\n")
              .append("        LOG \"Submitting artifacts to malware sandbox\"\n")
              .append("        DO submitToSandbox(artifacts: incident.collected_artifacts, sandbox: \"internal_cuckoo\", profile: \"full_behavioral\")\n")
              .append("        WAIT ack FROM malware_analyst TIMEOUT 45min\n")
              .append("        ON_TIMEOUT:\n")
              .append("            LOG \"Sandbox timeout — escalating for manual review\"\n")
              .append("            GOTO escalate\n")
              .append("        iocReport = getSandboxReport(incident_id: incident.id)\n")
              .append("        PARALLEL:\n")
              .append("            DO updateIOCFeed(source: iocReport)\n")
              .append("            DO updateEDRSignatures(source: iocReport)\n")
              .append("            DO updateSIEMRules(profile: \"T1055_injection_derived\")\n")
              .append("        GOTO resolve\n\n");
        }

        // escalate (always)
        pb.append("    PHASE escalate:\n")
          .append("        PARALLEL:\n")
          .append("            DO notifyManager(level: CRITICAL)\n")
          .append("            DO createTicket(priority: CRITICAL, type: \"injection_escalation\")\n")
          .append("            DO alertOnCall(service: \"incident_commander\", priority: CRITICAL)\n")
          .append("        SEVERITY CRITICAL\n")
          .append("        LOG \"Escalated — incident commander engaged\"\n\n");

        // resolve
        pb.append("    PHASE resolve:\n");
        if (integrity == 1) {
            pb.append("        LOG \"Host integrity verified — restoring to production\"\n")
              .append("        DO restoreHost(target: incident.affected_host, baseline: \"golden_image\")\n");
        } else if (integrity == 2) {
            pb.append("        LOG \"Host integrity compromised — scheduling re-image\"\n")
              .append("        DO scheduleReimage(host: incident.affected_host, priority: HIGH)\n");
        } else {
            pb.append("        LOG \"Host integrity unknown — escalating for manual review\"\n")
              .append("        GOTO escalate\n");
        }
        pb.append("        SEVERITY LOW\n")
          .append("        LOG \"Process injection incident resolved\"\n\n");

        // report_close (always — needed for VERIFY ON_FAIL in triage)
        pb.append("    PHASE report_close:\n")
          .append("        LOG \"Alert closed as false positive after validation\"\n\n");

        // REPORT
        pb.append("    REPORT:\n")
          .append("        DO closeTicket(id: incident.id, resolution: \"injection_remediated\")\n")
          .append("        DO generateReport(template: \"T1055_postmortem\", output: \"pdf\")\n")
          .append("        DO exportIOCs(format: \"stix2\", destination: \"threat_intel_platform\")\n")
          .append("        DO updateRunbook(playbook: \"T1055\", version: \"auto_increment\")\n")
          .append("        SEVERITY LOW\n")
          .append("        LOG \"T1055 Process Injection playbook completed\"\n");

        return pb.toString();
    }

    private String buildInjectionFalsePositive(String severity) {
        return "CONFIG team:\n"
             + "    lead: \"soc-lead\"\n"
             + "    members: [\"analyst_tier2\", \"forensics\"]\n"
             + "    escalation_chain: tier2 -> incident_commander\n\n"
             + "PLAYBOOK ProcessInjectionResponse\n"
             + "    TRIGGER: incident.severity >= " + severity + "\n\n"
             + "    PHASE triage:\n"
             + "        LOG \"T1055 alert — initiating triage\"\n"
             + "        edrAlert = validateEDRAlert(host: incident.affected_host, alert_id: incident.alert_id)\n"
             + "        VERIFY edrAlert\n"
             + "        ON_FAIL:\n"
             + "            LOG \"EDR alert could not be validated — closing as false positive\"\n"
             + "            SEVERITY LOW\n"
             + "            GOTO report_close\n"
             + "        LOG \"Alert validated — no further indicators found\"\n\n"
             + "    PHASE report_close:\n"
             + "        LOG \"Alert closed as false positive after validation\"\n\n"
             + "    REPORT:\n"
             + "        DO closeTicket(id: incident.id, resolution: \"false_positive\")\n"
             + "        SEVERITY LOW\n"
             + "        LOG \"T1055 alert closed — false positive confirmed\"\n";
    }

    // ================================================================== parse + report

    private void generateAndSaveReport(String source, String reportFile) throws IOException {
        CharStream        input  = CharStreams.fromString(source);
        IncidentFlowLexer  lexer  = new IncidentFlowLexer(input);
        CommonTokenStream  tokens = new CommonTokenStream(lexer);
        IncidentFlowParser parser = new IncidentFlowParser(tokens);

        SilentErrors errs = new SilentErrors();
        lexer.removeErrorListeners();  lexer.addErrorListener(errs);
        parser.removeErrorListeners(); parser.addErrorListener(errs);

        ParseTree tree = parser.program();

        if (errs.hasErrors()) {
            System.err.println(RED + "  ✗ " + RESET + "Internal playbook generation error:");
            for (String e : errs.list) System.err.println("      " + e);
            return;
        }

        SemanticChecker checker = new SemanticChecker();
        checker.visit(tree);
        if (checker.hasErrors()) {
            System.err.println(RED + "  ✗ " + RESET + "Semantic errors in generated playbook:");
            for (String e : checker.getErrors()) System.err.println("      " + e);
            return;
        }

        ReportVisitor rv = new ReportVisitor();
        rv.visit(tree);
        Files.write(Paths.get(reportFile), rv.getReport().getBytes());
        System.out.println(GREEN + "  ✓ " + RESET + "Report saved:    " + BOLD + reportFile + RESET);
    }

    // ================================================================== input helpers

    /** Numbered menu — returns 1-based choice. */
    private int menu(String question, String[] options) {
        System.out.println(BOLD + "  " + question + RESET);
        for (int i = 0; i < options.length; i++) {
            System.out.println(CYAN + "    " + (i + 1) + ") " + RESET + options[i]);
        }
        while (true) {
            System.out.print("  > ");
            try {
                int c = Integer.parseInt(in.nextLine().trim());
                if (c >= 1 && c <= options.length) { System.out.println(); return c; }
            } catch (NumberFormatException ignored) {}
            System.out.println(YELLOW + "  Please enter a number between 1 and " + options.length + RESET);
        }
    }

    /** Yes/No prompt — returns true for yes. */
    private boolean yesNo(String question) {
        System.out.println(BOLD + "  " + question + RESET);
        System.out.println(CYAN + "    1) " + RESET + "Yes");
        System.out.println(CYAN + "    2) " + RESET + "No");
        while (true) {
            System.out.print("  > ");
            String l = in.nextLine().trim().toLowerCase();
            if (l.equals("1") || l.equals("yes") || l.equals("y")) { System.out.println(); return true; }
            if (l.equals("2") || l.equals("no")  || l.equals("n")) { System.out.println(); return false; }
            System.out.println(YELLOW + "  Enter 1 (yes) or 2 (no)" + RESET);
        }
    }

    /** Severity picker — returns LOW / MEDIUM / HIGH / CRITICAL. */
    private String severity(String question) {
        int c = menu(question, new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
        return new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"}[c - 1];
    }

    /** Free-text prompt with a default value. */
    private String prompt(String question, String def) {
        System.out.print(BOLD + "  " + question + RESET + " [" + DIM + def + RESET + "]: ");
        String l = in.nextLine().trim();
        return l.isEmpty() ? def : l;
    }

    private void header(String title) {
        System.out.println(BOLD + "  " + title + RESET);
        System.out.println(DIM  + "  Answer each question to configure the playbook." + RESET);
        System.out.println();
    }

    // ================================================================== silent error listener

    private static class SilentErrors extends BaseErrorListener {
        final List<String> list = new ArrayList<>();
        @Override
        public void syntaxError(Recognizer<?, ?> r, Object sym, int line, int col,
                                String msg, RecognitionException e) {
            list.add("line " + line + ":" + col + " " + msg);
        }
        boolean hasErrors() { return !list.isEmpty(); }
    }
}
