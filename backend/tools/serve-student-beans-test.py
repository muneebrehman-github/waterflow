#!/usr/bin/env python3
"""
Serve the Student Beans iframe test page on https://www.lycamobile.us

Student Beans only allows iframe embedding from approved parent origins:
  - https://www.lycamobile.us
  - https://www.lycamobile.co.uk
  - https://stage.ldsvcplatform.com
  - https://cms-modern.studentbeans.com

localhost is blocked by Content-Security-Policy frame-ancestors, so the iframe
stays blank unless the parent page is served from an allowed domain.

Setup (run PowerShell as Administrator once):
  1. Add hosts entry:
       127.0.0.1 www.lycamobile.us
  2. Install deps (first run only):
       pip install cryptography

Usage:
  python serve-student-beans-test.py

Then open:
  https://www.lycamobile.us/student-beans-iframe-test.html
"""

from __future__ import annotations

import argparse
import datetime
import ipaddress
import os
import socket
import ssl
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

HOST = "www.lycamobile.us"
ALT_HOST = "lycamobile.us"
BIND_ADDRESS = "127.0.0.1"
PORT = 443
CERT_DIR = Path(__file__).resolve().parent / ".local-certs"
CERT_FILE = CERT_DIR / "lycamobile.us.pem"
KEY_FILE = CERT_DIR / "lycamobile.us-key.pem"


def ensure_cert() -> None:
    if CERT_FILE.exists() and KEY_FILE.exists():
        return

    try:
        from cryptography import x509
        from cryptography.hazmat.primitives import hashes, serialization
        from cryptography.hazmat.primitives.asymmetric import rsa
        from cryptography.x509.oid import NameOID
    except ImportError as exc:
        raise SystemExit(
            "Missing dependency. Install with: pip install cryptography"
        ) from exc

    CERT_DIR.mkdir(parents=True, exist_ok=True)

    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    subject = issuer = x509.Name(
        [
            x509.NameAttribute(NameOID.COUNTRY_NAME, "US"),
            x509.NameAttribute(NameOID.ORGANIZATION_NAME, "Local Dev"),
            x509.NameAttribute(NameOID.COMMON_NAME, HOST),
        ]
    )

    san = x509.SubjectAlternativeName(
        [
            x509.DNSName(HOST),
            x509.DNSName("lycamobile.us"),
            x509.IPAddress(ipaddress.IPv4Address("127.0.0.1")),
        ]
    )

    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.datetime.utcnow())
        .not_valid_after(datetime.datetime.utcnow() + datetime.timedelta(days=825))
        .add_extension(san, critical=False)
        .sign(key, hashes.SHA256())
    )

    KEY_FILE.write_bytes(
        key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.TraditionalOpenSSL,
            encryption_algorithm=serialization.NoEncryption(),
        )
    )

    CERT_FILE.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
    print(f"Generated self-signed cert: {CERT_FILE}")


def hosts_configured() -> bool:
    ok = True
    for name in (HOST, ALT_HOST):
        try:
            resolved = socket.gethostbyname(name)
            if not resolved.startswith("127."):
                print(f"WARNING: {name} resolves to {resolved}, expected 127.x.x.x")
                ok = False
        except OSError:
            print(f"WARNING: cannot resolve {name}")
            ok = False
    return ok


class StudentBeansTestHandler(SimpleHTTPRequestHandler):
    def _redirect_to_www(self) -> None:
        host = self.headers.get("Host", "")
        if host.split(":")[0] == ALT_HOST:
            target = f"https://{HOST}{self.path}"
            self.send_response(301)
            self.send_header("Location", target)
            self.end_headers()
            return True
        return False

    def do_GET(self) -> None:
        if self._redirect_to_www():
            return
        return super().do_GET()

    def do_HEAD(self) -> None:
        if self._redirect_to_www():
            return
        return super().do_HEAD()

    def log_message(self, format: str, *args) -> None:
        print(f"[{self.log_date_time_string()}] {format % args}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve Student Beans test page on lycamobile.us")
    parser.add_argument("--port", type=int, default=PORT, help="HTTPS port (default: 443)")
    args = parser.parse_args()

    tools_dir = Path(__file__).resolve().parent
    os.chdir(tools_dir)

    if not hosts_configured():
        print("WARNING: hosts file may not be configured.")
        print("Add these lines to C:\\Windows\\System32\\drivers\\etc\\hosts (as Administrator):")
        print(f"  127.0.0.1 {HOST}")
        print(f"  127.0.0.1 {ALT_HOST}")
        print()

    ensure_cert()

    server = ThreadingHTTPServer((BIND_ADDRESS, args.port), StudentBeansTestHandler)
    context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    context.load_cert_chain(certfile=CERT_FILE, keyfile=KEY_FILE)
    server.socket = context.wrap_socket(server.socket, server_side=True)

    url = f"https://{HOST}:{args.port}/student-beans-iframe-test.html"
    if args.port == 443:
        url = f"https://{HOST}/student-beans-iframe-test.html"

    print("Serving Student Beans iframe test page")
    print(f"  Directory: {tools_dir}")
    print(f"  URL:       {url}")
    print()
    print("Your browser will warn about the self-signed certificate — accept for local testing.")
    print("Press Ctrl+C to stop.")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopped.")
        server.server_close()


if __name__ == "__main__":
    main()
