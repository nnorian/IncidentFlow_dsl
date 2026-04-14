# CLI reference

The `iflow` command is the main interface to the IncidentFlow toolchain.

## Installation

From the project root, run once:

```
bash install.sh
```

Then reload your shell (`source ~/.bashrc` or open a new terminal). After that, `iflow` is available from any directory.

If you do not want a system-wide install, you can also run the wrapper directly from the project root:

```
./iflow <command>
```

---

## Commands

### wizard

```
iflow wizard
```

Starts an interactive session that asks you questions about an incident and generates two output files: a `.iflow` playbook and a `_report.md` incident report. Both files are written to your current directory.

The wizard supports three attack techniques:

- T1566 — Phishing
- T1059.001 — PowerShell Execution
- T1055 — Process Injection

You will be asked to choose a technique, give the output a base name, and then answer a series of questions about what was observed. Answers are entered by typing a number and pressing Enter.

Example session:

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
    1) Yes  2) No
  > 1

  Was credential harvesting detected?
    1) Yes  2) No
  > 2

  Are any endpoints suspected to be compromised?
    1) Yes  2) No
  > 1

  Playbook saved:  phishing_incident.iflow
  Report saved:    phishing_incident_report.md
```

---

### check

```
iflow check <file.iflow>
```

Validates the given file for syntax and semantic errors. Exits with code 0 if clean, 2 on syntax errors, 3 on semantic errors.

Currently checked semantics: all `GOTO` targets must reference phases that exist in the same playbook.

```
$ iflow check playbooks/sample.iflow
  → Checking playbooks/sample.iflow ...
  ✓ No errors found.
```

---

### report

```
iflow report <file.iflow> [-o output.md]
```

Validates the file and, if it is clean, generates a Markdown incident report from it. The report includes configuration, per-phase statement breakdowns, a severity trail, all actions performed, and all log entries.

Without `-o`, the report is printed to stdout. With `-o`, it is written to the given file.

```
iflow report playbooks/phishing.iflow -o out.md
iflow report playbooks/phishing.iflow            # prints to terminal
```

---

### tree

```
iflow tree <file.iflow>
```

Opens a GUI window showing the parse tree of the given file. Requires a display (does not work over a headless SSH session). Useful for understanding how the parser sees a file when debugging grammar issues.

---

### serve

```
iflow serve [-p port] [-d directory]
```

Starts the HTTP API server. See [API.md](API.md) for the full endpoint reference.

Options:

- `-p` — port to listen on (default: 8080)
- `-d` — directory to serve playbooks from (default: current directory)

```
iflow serve
iflow serve -p 9000
iflow serve -p 9000 -d playbooks/
```

The server runs until you press Ctrl+C.

---

## Options

```
-v, --version    print version and exit
-h, --help       show help and exit
```

---

## Language quick reference

A `.iflow` file has the following structure:

```
CONFIG <name>:
    <key>: <value>
    ...

IMPORT "<path>" AS <alias>

PLAYBOOK <Name>
    TRIGGER: <expression>

    PHASE <name>:
        <statements>

    REPORT:
        <statements>
```

Statements available inside a phase:

| Statement | Syntax | Description |
|---|---|---|
| Assign | `x = someFunction()` | Store a return value |
| Do | `DO someFunction(arg: val)` | Call a function, discard result |
| Log | `LOG "message"` | Record a message in the report |
| Severity | `SEVERITY HIGH` | Set the current severity level |
| Goto | `GOTO phaseName` | Jump to another phase |
| If | `IF expr THEN: ... ELSE: ...` | Conditional branching |
| For | `FOR EACH x IN collection: ...` | Iterate over a collection |
| Parallel | `PARALLEL: ...` | Run a block of statements concurrently |
| Verify | `VERIFY expr` followed by `ON_FAIL: ...` | Assert a condition, handle failure |
| Wait | `WAIT ack FROM source TIMEOUT 15min` followed by `ON_TIMEOUT: ...` | Block until acknowledged |

Severity levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

Function arguments use named syntax: `someFunction(key: value, other: value)`

Member access uses dot notation: `incident.severity`, `node.owner`

Comments use `#` for single-line and `/* */` for blocks.
