#!/usr/bin/env bash
# UI test harness for Atlas
# Usage: test/ui-test.sh
# Runs every test/cases/*.in against Atlas and checks the expected substrings.
# Fails fast on the first failing case (per the course test-ui skill spec).

set -u
cd "$(dirname "$0")/.."

# Compile to a temp dir so the repo never accumulates .class files
BIN=$(mktemp -d)
trap 'rm -rf "$BIN"' EXIT

if ! javac -Xlint:none -d "$BIN" src/main/java/*.java; then
    echo "********** BUILD FAILURE **********"
    exit 1
fi

pass=0

for input in test/cases/*.in; do
    name=$(basename "$input" .in)
    expected_file="test/cases/$name.expected"
    output=$(java -cp "$BIN" Atlas < "$input" 2>&1)
    ok=1
    while IFS= read -r expected; do
        [ -z "$expected" ] && continue
        if ! grep -qF -- "$expected" <<<"$output"; then
            echo "[FAIL] $name: expected to find: $expected"
            echo "----- console input -----"
            cat "$input"
            echo "----- actual output -----"
            echo "$output"
            echo "********** TEST SESSION TERMINATED **********"
            exit 1
        fi
    done < "$expected_file"
    pass=$((pass + 1))
    echo "[PASS] $name"
done

echo "--------------------------------"
echo "All $pass case(s) passed."
