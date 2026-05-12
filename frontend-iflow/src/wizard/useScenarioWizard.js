import { useMemo, useState } from "react";

import {
  buildWizardPhishingSource,
  buildWizardPowerShellSource,
  buildWizardProcessInjectionSource,
} from "./builders";
import { sanitizeBaseName } from "../utils/fileTransfer";

export const useScenarioWizard = () => {
  const [wizardAttack, setWizardAttack] = useState(1);
  const [wizardBaseName, setWizardBaseName] = useState("incident");
  const [wizardSeverity, setWizardSeverity] = useState("HIGH");

  // Phishing
  const [phishingEmailType, setPhishingEmailType] = useState(1);
  const [phishingPayloadExec, setPhishingPayloadExec] = useState(false);
  const [phishingUrlVisited, setPhishingUrlVisited] = useState(false);
  const [phishingCredHarvest, setPhishingCredHarvest] = useState(false);
  const [phishingEndpointSuspected, setPhishingEndpointSuspected] =
    useState(false);
  const [phishingNeedsEscalate, setPhishingNeedsEscalate] = useState(false);

  // PowerShell
  const [psLoggingEnabled, setPsLoggingEnabled] = useState(true);
  const [psRiskLevel, setPsRiskLevel] = useState(2);
  const [psAmsiBypass, setPsAmsiBypass] = useState(false);
  const [psLateralMove, setPsLateralMove] = useState(false);
  const [psCredDump, setPsCredDump] = useState(false);
  const [psExfil, setPsExfil] = useState(false);

  // Process Injection
  const [injAlertValid, setInjAlertValid] = useState(true);
  const [injInjectionType, setInjInjectionType] = useState(1);
  const [injPrivileged, setInjPrivileged] = useState(false);
  const [injC2Detected, setInjC2Detected] = useState(false);
  const [injPersistenceFound, setInjPersistenceFound] = useState(false);
  const [injLateralMove, setInjLateralMove] = useState(false);
  const [injSandboxNeeded, setInjSandboxNeeded] = useState(false);
  const [injIntegrity, setInjIntegrity] = useState(1);

  const wizardTechniqueTitle = useMemo(() => {
    if (wizardAttack === 1) return "T1566 — Phishing Response";
    if (wizardAttack === 2) return "T1059.001 — PowerShell Execution Response";
    if (wizardAttack === 3) return "T1055 — Process Injection Response";
    return "Scenario";
  }, [wizardAttack]);

  const phishingHasAttachment = phishingEmailType === 1 || phishingEmailType === 3;
  const phishingHasLink = phishingEmailType === 2 || phishingEmailType === 3;

  const phishingEffectivePayloadExec = phishingHasAttachment
    ? phishingPayloadExec
    : false;
  const phishingEffectiveUrlVisited = phishingHasLink ? phishingUrlVisited : false;
  const phishingEffectiveCredHarvest =
    phishingHasLink && phishingEffectiveUrlVisited ? phishingCredHarvest : false;

  const showPhishingEndpointSuspected =
    !phishingEffectivePayloadExec &&
    phishingEffectiveUrlVisited &&
    !phishingEffectiveCredHarvest;

  const generateSource = () => {
    if (wizardAttack === 1) {
      return buildWizardPhishingSource({
        severity: wizardSeverity,
        emailType: phishingEmailType,
        payloadExec: phishingPayloadExec,
        urlVisited: phishingUrlVisited,
        credHarvest: phishingCredHarvest,
        endpointSuspected: phishingEndpointSuspected,
        needsEscalate: phishingNeedsEscalate,
      });
    }

    if (wizardAttack === 2) {
      return buildWizardPowerShellSource({
        severity: wizardSeverity,
        loggingEnabled: psLoggingEnabled,
        riskLevel: psRiskLevel,
        amsiBypass: psAmsiBypass,
        lateralMove: psLateralMove,
        credDump: psCredDump,
        exfil: psExfil,
      });
    }

    return buildWizardProcessInjectionSource({
      severity: wizardSeverity,
      alertValid: injAlertValid,
      injectionType: injInjectionType,
      privileged: injPrivileged,
      c2Detected: injC2Detected,
      persistenceFound: injPersistenceFound,
      lateralMove: injLateralMove,
      sandboxNeeded: injSandboxNeeded,
      integrity: injIntegrity,
    });
  };

  const getExportBaseName = () => {
    const trimmed = (wizardBaseName ?? "").trim();
    if (!trimmed) return "incident";
    return sanitizeBaseName(trimmed, true);
  };

  return {
    // General
    wizardAttack,
    setWizardAttack,
    wizardBaseName,
    setWizardBaseName,
    wizardSeverity,
    setWizardSeverity,

    // Phishing
    phishingEmailType,
    setPhishingEmailType,
    phishingPayloadExec,
    setPhishingPayloadExec,
    phishingUrlVisited,
    setPhishingUrlVisited,
    phishingCredHarvest,
    setPhishingCredHarvest,
    phishingEndpointSuspected,
    setPhishingEndpointSuspected,
    phishingNeedsEscalate,
    setPhishingNeedsEscalate,

    // PowerShell
    psLoggingEnabled,
    setPsLoggingEnabled,
    psRiskLevel,
    setPsRiskLevel,
    psAmsiBypass,
    setPsAmsiBypass,
    psLateralMove,
    setPsLateralMove,
    psCredDump,
    setPsCredDump,
    psExfil,
    setPsExfil,

    // Process Injection
    injAlertValid,
    setInjAlertValid,
    injInjectionType,
    setInjInjectionType,
    injPrivileged,
    setInjPrivileged,
    injC2Detected,
    setInjC2Detected,
    injPersistenceFound,
    setInjPersistenceFound,
    injLateralMove,
    setInjLateralMove,
    injSandboxNeeded,
    setInjSandboxNeeded,
    injIntegrity,
    setInjIntegrity,

    // Derived
    wizardTechniqueTitle,
    phishingHasAttachment,
    phishingHasLink,
    showPhishingEndpointSuspected,

    // Actions
    generateSource,
    getExportBaseName,
  };
};
