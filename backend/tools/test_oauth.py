import json
import sys
import time
import urllib.parse
import urllib.request

import jwt

sa = json.load(open(sys.argv[1]))
now = int(time.time())
payload = {
    "iss": sa["client_email"],
    "scope": "https://www.googleapis.com/auth/firebase.messaging",
    "aud": sa["token_uri"],
    "iat": now,
    "exp": now + 3600,
}
assertion = jwt.encode(payload, sa["private_key"], algorithm="RS256")
data = urllib.parse.urlencode(
    {
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion": assertion,
    }
).encode()
req = urllib.request.Request(sa["token_uri"], data=data, method="POST")
resp = urllib.request.urlopen(req, timeout=15)
print("PY_OK", len(json.load(resp)["access_token"]))
