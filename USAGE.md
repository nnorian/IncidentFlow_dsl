# IncidentFlow — Usage Guide

## Setup

Run this once from the project root to build and install the tool:

```
bash install.sh
```

Then reload your shell:

```
source ~/.bashrc
```

---

## Generating a report with the wizard

This is the main way to use the tool. Run:

```
iflow wizard
```

You will be asked a series of questions about the incident. Answer by typing the number next to your choice and pressing Enter.

The wizard supports three attack techniques:

- T1566 — Phishing
- T1059.001 — PowerShell Execution
- T1055 — Process Injection

At the end, two files are created in your current directory:

- `<name>.iflow` — the generated playbook
- `<name>_report.md` — the incident report

---

## Validating a playbook manually

If you have an existing `.iflow` file and want to check it for errors:

```
iflow check myplaybook.iflow
```

---

## Generating a report from an existing playbook

```
iflow report myplaybook.iflow -o report.md
```

If you leave out `-o report.md`, the report is printed to the terminal instead.

---

## Commands summary

```
iflow wizard                        guided playbook and report generator
iflow check  <file>                 validate a playbook file
iflow report <file> [-o out.md]     generate a report from a playbook
iflow tree   <file>                 open the parse tree viewer (GUI)
iflow --version                     print version
iflow --help                        show help
```

---

## Example session

```
$ iflow wizard

  Select the attack technique:
    1) T1566     — Phishing
    2) T1059.001 — PowerShell Execution
    3) T1055     — Process Injection
  > 1

  Output base name: > phishing_incident

  What severity level triggered this alert?
    1) LOW  2) MEDIUM  3) HIGH  4) CRITICAL
  > 3

  What type of phishing was detected?
    1) Spearphishing attachment (T1566.001)
    2) Spearphishing link      (T1566.002)
    3) Unknown / investigate both
  > 2

  Was the malicious URL visited by any user?
    1) Yes
    2) No
  > 1

  Was credential harvesting detected?
    1) Yes
    2) No
  > 2

  Are any endpoints suspected to be compromised?
    1) Yes
    2) No
  > 1

  Is manual escalation to an incident commander required?
    1) Yes
    2) No
  > 2

  Playbook saved:  phishing_incident.iflow
  Report saved:    phishing_incident_report.md
```
