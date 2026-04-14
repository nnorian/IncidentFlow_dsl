# IncidentFlow

IncidentFlow is a domain-specific language for writing incident response playbooks. A playbook describes what to do when a security incident occurs — which phases to run, what actions to take, how to escalate, and when to close.

The toolchain reads `.iflow` files, validates them, and generates structured Markdown incident reports. There is also an interactive wizard that asks questions about an incident and writes a playbook for you.

## What it is not

IncidentFlow does not execute actions like blocking IPs or resetting passwords. It is a documentation and workflow-definition tool. The generated playbooks describe what a team should do; they do not do it themselves.

## Project layout

```
grammar/        the ANTLR grammar that defines the language
src/            Java source for the compiler, report generator, wizard, and API server
playbooks/      example .iflow files
assets/         diagrams and images
output/         generated reports land here (not committed)
lib/            ANTLR runtime jar (not committed, downloaded by build.sh)
gen/            parser generated from the grammar (not committed)
bin/            compiled classes (not committed)
build.sh        builds everything from source
install.sh      builds and installs the iflow command to ~/.local/bin
```

## Building

```
bash build.sh
```

This downloads the ANTLR jar if needed, generates the parser, compiles all sources, and writes the `iflow` wrapper script in the project root. It also runs a smoke test against `playbooks/sample.iflow`.

## Installing

```
bash install.sh
```

Runs the build and copies the `iflow` command to `~/.local/bin` so you can use it from anywhere.

## Quick start

```
./iflow wizard
```

Walks through an incident interactively and produces a `.iflow` playbook and a Markdown report.

```
./iflow check playbooks/sample.iflow
./iflow report playbooks/sample.iflow -o out.md
```

See [CLI.md](CLI.md) for full command reference and [API.md](API.md) for the HTTP API.

## Language overview

A playbook has configuration blocks, a trigger, and a sequence of named phases. Each phase contains statements: logging, variable assignment, conditional branching, iteration, parallel execution, waits, and `GOTO` jumps between phases. A `REPORT` block at the end describes how to close and document the incident.

```
CONFIG team:
    lead: "alice"
    members: ["bob", "carol"]
    escalation_chain: tier1 -> tier2 -> oncall

PLAYBOOK DatabaseOutage
    TRIGGER: incident.severity >= HIGH

    PHASE triage:
        status = pingDB()
        IF status == FALSE THEN:
            SEVERITY HIGH
            GOTO escalate
        ELSE:
            LOG "All checks passed"

    PHASE escalate:
        PARALLEL:
            DO notifyPager(user: oncall, priority: CRITICAL)
            DO takeSnapshot(scope: "db", format: "full")
        SEVERITY CRITICAL

    REPORT:
        DO closeTicket(id: incident.id, resolution: "auto")
        SEVERITY LOW
        LOG "Incident closed"
```

## Supported attack techniques (wizard)

The wizard generates playbooks for three MITRE ATT&CK techniques:

- T1566 — Phishing (attachment, link, credential harvesting, endpoint compromise)
- T1059.001 — PowerShell Execution (risk levels, AMSI bypass, lateral movement)
- T1055 — Process Injection (DLL, PE, process hollowing, APC injection)
