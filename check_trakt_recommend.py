import urllib.request
import json
url = "https://trakt.docs.apiary.io/def/api/recommendations.json"
try:
    with urllib.request.urlopen(url) as response:
        print(response.read().decode('utf-8')[:1000])
except Exception as e:
    print(f"Error: {e}")
