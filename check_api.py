import json
import urllib.request
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request("https://raw.githubusercontent.com/trakt/api-help/master/README.md")
try:
    with urllib.request.urlopen(req, context=ctx) as response:
        html = response.read().decode('utf-8')
        for line in html.split('\n'):
            if "progress" in line.lower() or "next" in line.lower() or "watched" in line.lower():
                print(line.strip())
except Exception as e:
    print(e)
