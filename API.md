# HTTP API reference

The HTTP API lets you interact with the IncidentFlow compiler over the network. It is useful for integrating with other tools, building a frontend, or running the compiler as a service.

## Starting the server

```
iflow serve -p 8080 -d playbooks/
```

The `-d` flag sets the directory the server reads `.iflow` files from when handling playbook endpoints. The server runs until you press Ctrl+C.

All responses are JSON with `Content-Type: application/json`. CORS headers are included on every response, so the API can be called from a browser.

---

## Endpoints

### GET /

Returns a description of the API and its endpoints.

```
curl http://localhost:8080/
```

Response:

```json
{
  "name": "IncidentFlow API",
  "version": "1.0.0",
  "endpoints": [...]
}
```

---

### GET /health

Returns `200 OK` when the server is running.

```
curl http://localhost:8080/health
```

Response:

```json
{ "status": "ok" }
```

---

### POST /api/compile

Validates `.iflow` source text for syntax and semantic errors. The request body must be a JSON object with a `source` field containing the playbook source as a string.

```
curl -X POST http://localhost:8080/api/compile \
  -H "Content-Type: application/json" \
  -d '{"source": "PLAYBOOK Test\n  TRIGGER: incident.severity >= HIGH\n  PHASE triage:\n    LOG \"started\"\n  REPORT:\n    LOG \"done\"\n"}'
```

Response when valid (status 200):

```json
{
  "valid": true,
  "syntax_errors": [],
  "semantic_errors": []
}
```

Response when invalid (status 422):

```json
{
  "valid": false,
  "syntax_errors": [
    "line 1:16 mismatched input 'THIS' expecting 'TRIGGER'"
  ],
  "semantic_errors": []
}
```

---

### POST /api/report

Validates `.iflow` source text and, if valid, returns the generated Markdown incident report. The request body is the same format as `/api/compile`.

```
curl -X POST http://localhost:8080/api/report \
  -H "Content-Type: application/json" \
  -d '{"source": "..."}'
```

Response when valid (status 200):

```json
{
  "report": "# IncidentFlow Incident Report\n\n**Generated:** 2026-04-14 ...\n..."
}
```

The `report` field contains the full Markdown text. If the source has errors, the response is the same error format as `/api/compile` with status 422.

---

### GET /api/playbooks

Lists the names of all `.iflow` files in the server's playbooks directory. Names are returned without the `.iflow` extension, sorted alphabetically.

```
curl http://localhost:8080/api/playbooks
```

Response:

```json
{ "playbooks": ["phishing", "powershell_exec", "process_injection", "sample"] }
```

---

### GET /api/playbooks/{name}

Returns the source of a stored playbook by name (without the `.iflow` extension).

```
curl http://localhost:8080/api/playbooks/sample
```

Response (status 200):

```json
{
  "name": "sample",
  "source": "CONFIG team:\n    lead: \"alice\"\n..."
}
```

If the playbook does not exist, the response is status 404:

```json
{ "error": "Playbook not found: nonexistent" }
```

---

### GET /api/playbooks/{name}/report

Validates a stored playbook and returns its generated Markdown report.

```
curl http://localhost:8080/api/playbooks/phishing/report
```

Response (status 200):

```json
{
  "name": "phishing",
  "report": "# IncidentFlow Incident Report\n\n..."
}
```

If the playbook does not exist, returns 404. If the playbook has errors, returns 422 with the error list.

---

## Error responses

All error responses follow the same shape:

```json
{ "error": "description of what went wrong" }
```

Validation errors (from `/api/compile` and `/api/report`) use a different shape that includes the error lists:

```json
{
  "valid": false,
  "syntax_errors": ["..."],
  "semantic_errors": ["..."]
}
```

## Status codes used

| Code | Meaning |
|------|---------|
| 200 | Success |
| 204 | CORS preflight response |
| 400 | Bad request (e.g. missing `source` field) |
| 404 | Playbook not found |
| 405 | Method not allowed |
| 422 | Source has validation errors |
| 500 | Internal server error |

## Security notes

The server does not require authentication. Run it on localhost or inside a trusted network only.

Path traversal is blocked on the playbook endpoints — requests like `/api/playbooks/../build.sh` are rejected.
