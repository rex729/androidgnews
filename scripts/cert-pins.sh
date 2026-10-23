#!/usr/bin/env bash
# Prints the SSL-pinning hash (SHA-256 of the public key, OkHttp's "sha256/..." format) of every
# certificate a server presents, leaf first. Usage: scripts/cert-pins.sh backend.example.com [port]
set -euo pipefail

host="${1:?usage: $0 host [port]}"
port="${2:-443}"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

echo | openssl s_client -servername "$host" -connect "$host:$port" -showcerts 2>/dev/null \
  | awk '/BEGIN CERTIFICATE/,/END CERTIFICATE/' > "$work/chain.pem"
[ -s "$work/chain.pem" ] || { echo "No certificates received from $host:$port" >&2; exit 1; }

csplit -s -z -f "$work/cert-" "$work/chain.pem" '/-----BEGIN CERTIFICATE-----/' '{*}'
for cert in "$work"/cert-*; do
  subject="$(openssl x509 -in "$cert" -noout -subject | sed 's/^subject=//')"
  pin="$(openssl x509 -in "$cert" -pubkey -noout \
    | openssl pkey -pubin -outform der \
    | openssl dgst -sha256 -binary \
    | openssl enc -base64)"
  echo "sha256/$pin   $subject"
done
