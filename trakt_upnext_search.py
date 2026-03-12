import urllib.request
import urllib.parse
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

data = urllib.parse.urlencode({'q': 'trakt api endpoint "up next" to watch'}).encode('utf-8')
req = urllib.request.Request("https://lite.duckduckgo.com/lite/", data=data, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req, context=ctx) as response:
        html = response.read().decode('utf-8')
        print(html[:2000])
except Exception as e:
    print(e)
