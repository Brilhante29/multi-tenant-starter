param(
  [string]$ResultsDir = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  $trackedChanges = git status --porcelain
  if ($trackedChanges) {
    throw "Commit tracked source changes before producing benchmark evidence."
  }

  $env:SOURCE_COMMIT = (git rev-parse HEAD).Trim()
  $env:CLEAN_TREE = "true"
  $env:DEPENDENCY_LOCK_DIGEST = "sha256:" + (Get-FileHash gradle.lockfile -Algorithm SHA256).Hash.ToLowerInvariant()
  $env:BENCHMARK_PRODUCER = if ($env:GITHUB_ACTIONS -eq "true") { "github-actions" } else { "local" }
  if ([string]::IsNullOrWhiteSpace($ResultsDir)) {
    $ResultsDir = Join-Path $root "benchmarks/results"
  } elseif (-not [System.IO.Path]::IsPathRooted($ResultsDir)) {
    $ResultsDir = Join-Path $root $ResultsDir
  }
  New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
  $env:BENCHMARK_RESULTS_DIR = (Resolve-Path -LiteralPath $ResultsDir).Path

  docker compose -p multi-tenant-starter-benchmark build app
  if ($LASTEXITCODE -ne 0) { throw "Docker image build failed." }
  $env:APP_IMAGE_DIGEST = (docker image inspect multi-tenant-starter:benchmark --format '{{.Id}}').Trim()
  docker pull postgres:17.6-alpine | Out-Null
  if ($LASTEXITCODE -ne 0) { throw "PostgreSQL image pull failed." }
  $env:POSTGRES_IMAGE_DIGEST = (docker image inspect postgres:17.6-alpine --format '{{.Id}}').Trim()

  docker compose -p multi-tenant-starter-benchmark run --rm app benchmark
  if ($LASTEXITCODE -ne 0) { throw "Benchmark failed." }
  $env:EXPECTED_SOURCE_COMMIT = $env:SOURCE_COMMIT
  $env:BENCHMARK_RESULT_PATH = Join-Path $env:BENCHMARK_RESULTS_DIR "multi-tenant-starter-v2.json"
  python tools/validate-benchmark.py
  if ($LASTEXITCODE -ne 0) { throw "Benchmark validation failed." }
} finally {
  $cleanupPreference = $ErrorActionPreference
  $ErrorActionPreference = "SilentlyContinue"
  docker compose -p multi-tenant-starter-benchmark down --remove-orphans *> $null
  $ErrorActionPreference = $cleanupPreference
  $global:LASTEXITCODE = 0
  Pop-Location
}
