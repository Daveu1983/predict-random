# MLOps vs DevOps: Separated Pipelines Demo

This repository demonstrates a clean separation between an **MLOps pipeline** (which trains and publishes a model) and a **DevOps pipeline** (which builds and ships application services). The two pipelines are independently triggered, produce independent artifacts, and can be released on independent schedules — while still cooperating end-to-end.

## What the system does

A polyglot set of microservices generates random numbers. One orchestrator service collects the data, stores it, and publishes it for training. A separate training service consumes the data, fits a statistical model, and publishes the model. The orchestrator then loads that model at runtime to score its own outputs.

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ python-random│    │ golang-random│    │  js-random   │
│  Flask :5000 │    │ stdlib :8081 │    │ Express :8083│
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │ (ClusterIP + NetworkPolicy:
                           │  only getting-random may call)
                           ▼
                  ┌──────────────────┐
                  │  getting-random  │  Java 21 / Quarkus + Camel
                  │  Orchestrator    │  100× calls per source = 300 samples
                  │  :8080 (LB)      │  → CSV → GCS + Kafka "random-data"
                  │  Loads model for │
                  │  inference/zscore│
                  └────────┬─────────┘
                           │ Kafka
                           ▼
                  ┌──────────────────┐
                  │training-random-  │  Java 17 / Quarkus + Kafka
                  │     check        │  Consumes CSV → trains model
                  │  (MLOps service) │  → writes random-model.json
                  └──────────────────┘
```

## Services

| Service | Lang / Framework | Role |
|---|---|---|
| `python-random` | Python 3.8 / Flask | Random number generator (1–100) |
| `golang-random` | Go 1.20 / stdlib | Random number generator (1–100) |
| `js-random` | Node.js 24 / Express | Random number generator (1–100) |
| `getting-random` | Java 21 / Quarkus + Camel | Orchestrator; loads the trained model and scores outputs |
| `training-random-check` | Java 17 / Quarkus + Kafka | MLOps training consumer; produces `random-model.json` |

All three random services require the `X-App-Name: getting-random` header (else `403`). In Kubernetes, only `getting-random` has a LoadBalancer and only its pod is permitted inbound to the generators via NetworkPolicy.

## How MLOps and DevOps are separated

Two GitHub Actions workflows live in [.github/workflows/](.github/workflows/):

### [mlops.yml](.github/workflows/mlops.yml) — MLOps pipeline
- **Triggers only on changes under** [training-random-check/](training-random-check/) (plus manual dispatch).
- Builds the training service, trains a model, and publishes:
  - `random-model.json` as a GitHub Release tagged `model-<timestamp>-<run>`
  - The training service image to GHCR
  - The training CSV as a workflow artifact (30-day retention)
- **Does not build or touch the serving services.**

### [devops.yml](.github/workflows/devops.yml) — DevOps pipeline
- **Triggers on any change *outside* [training-random-check/](training-random-check/)** (plus manual dispatch, plus publication of any `model-*` GitHub Release so a fresh model reaches serving automatically).
- Downloads the latest `model-*` GitHub Release, bakes `random-model.json` into the `getting-random` image, and builds+publishes all four serving images to GHCR.
- **Does not train.** If no model release exists yet, it ships an empty placeholder so the service still boots.

### Why this counts as separation

| Property | How it's enforced |
|---|---|
| Independent triggers | Path filters on `push` events; `workflow_dispatch` for either alone |
| Independent artifacts | MLOps → GitHub Release (`random-model.json`); DevOps → OCI images on GHCR |
| Independent cadence | Retrain without rebuilding services; rebuild services without retraining |
| Graceful decoupling | [ModelLoader.java](getting-random/src/main/java/com/service/ModelLoader.java) logs and skips scoring if the model is missing — serving never hard-depends on a training run |
| Coordinated when it matters | Publishing a `model-*` Release triggers DevOps via the `release` event, so new models reach production without manual glue |

## Verifying the separation

1. **Trigger isolation** — Commit a whitespace change under [training-random-check/](training-random-check/): only `mlops.yml` runs. Commit one under [getting-random/](getting-random/): only `devops.yml` runs.
2. **Retrain without redeploy** — `workflow_dispatch` on MLOps. A new `model-*` release appears; no new serving image SHA is pushed. Restart the `getting-random` pod and it loads the new model.
3. **Redeploy without retrain** — `workflow_dispatch` on DevOps. New service images are pushed; the existing latest model release is baked in unchanged; no training job runs.
4. **Failure containment** — A failing training run never publishes a `model-*` Release, so no downstream DevOps run is triggered. Manual DevOps dispatch remains available.

## Running locally

```bash
docker compose up --build
curl http://localhost:8080/random   # triggers the orchestrator
```

In dev mode, `getting-random` writes CSV to local disk and `training-random-check` writes the model to a local file. In prod (`env=prd`), both read/write `gs://random-data-bucket/`.

## Kubernetes

Each service ships a Deployment + Service under `{service}/kubernetes/`. Only `getting-random` is exposed externally; NetworkPolicies restrict the generators to in-cluster calls from the orchestrator pod.
