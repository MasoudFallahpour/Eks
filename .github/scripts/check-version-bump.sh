#!/usr/bin/env bash
set -euo pipefail

if [ $# -ne 2 ]; then
  echo "Usage: $0 <old-rev> <new-rev>" >&2
  exit 2
fi

old_rev=$1
new_rev=$2

fail() {
  if [ -n "${GITHUB_ACTIONS:-}" ]; then
    echo "::error file=$1::$2"
  else
    echo "Error ($1): $2" >&2
  fi
  exit 1
}

output() {
  echo "$1=$2"
  if [ -n "${GITHUB_OUTPUT:-}" ]; then
    echo "$1=$2" >> "$GITHUB_OUTPUT"
  fi
}

read_version() {
  git show "${1}:app/build.gradle.kts" | sed -nE "s/^ *$2 = \"?([^\"]+)\"?$/\1/p"
}

for rev in "$old_rev" "$new_rev"; do
  if ! git cat-file -e "${rev}^{commit}" 2> /dev/null; then
    fail "app/build.gradle.kts" "Commit $rev not found"
  fi
done

old_code=$(read_version "$old_rev" versionCode)
new_code=$(read_version "$new_rev" versionCode)
old_name=$(read_version "$old_rev" versionName)
new_name=$(read_version "$new_rev" versionName)

if ! [[ $old_code =~ ^[0-9]+$ && $new_code =~ ^[0-9]+$ && -n $old_name && -n $new_name ]]; then
  fail "app/build.gradle.kts" "Could not read versionCode/versionName"
fi

output version-name "$new_name"

if [ "$new_code" -eq "$old_code" ]; then
  echo "versionCode unchanged ($new_code): no release."
  output release false
  exit 0
fi

if [ "$new_code" -lt "$old_code" ]; then
  fail "app/build.gradle.kts" "versionCode decreased from $old_code to $new_code"
fi

if [ "$new_name" = "$old_name" ]; then
  fail "app/build.gradle.kts" "versionCode was bumped but versionName is still $new_name"
fi

if ! git show "${new_rev}:CHANGELOG.md" | grep -qxF "# Version $new_name"; then
  fail "CHANGELOG.md" "CHANGELOG.md has no '# Version $new_name' section"
fi

tag_status=0
git ls-remote --exit-code --tags origin "refs/tags/v$new_name" > /dev/null || tag_status=$?
case $tag_status in
  0) fail "app/build.gradle.kts" "Tag v$new_name already exists" ;;
  2) ;;
  *) fail "app/build.gradle.kts" "Could not query tags on origin" ;;
esac

echo "versionCode $old_code -> $new_code: release $new_name."
output release true
