#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

if [ -n "$(git status --porcelain --untracked-files=no)" ]; then
  echo "Commit tracked source changes before producing benchmark evidence." >&2
  exit 1
fi

export SOURCE_COMMIT="$(git rev-parse HEAD)"
export CLEAN_TREE=true
export DEPENDENCY_LOCK_DIGEST="sha256:$(sha256sum gradle.lockfile | cut -d ' ' -f 1)"
if [ "${GITHUB_ACTIONS:-false}" = "true" ]; then
  export BENCHMARK_PRODUCER=github-actions
else
  export BENCHMARK_PRODUCER=local
fi

cleanup() {
  docker compose -p multi-tenant-starter-benchmark down --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker compose -p multi-tenant-starter-benchmark build app
export APP_IMAGE_DIGEST="$(docker image inspect multi-tenant-starter:benchmark --format '{{.Id}}')"
docker pull postgres:17.6-alpine >/dev/null
export POSTGRES_IMAGE_DIGEST="$(docker image inspect postgres:17.6-alpine --format '{{.Id}}')"
docker compose -p multi-tenant-starter-benchmark run --rm app benchmark
EXPECTED_SOURCE_COMMIT="$SOURCE_COMMIT" python3 tools/validate-benchmark.py
