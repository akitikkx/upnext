# Upnext: TV Series Manager
Have you ever been frustrated by your favorite series' 
production breaks to the point where you find it difficult 
to keep track of when the series will come back? This app aims 
to provide you with that information about the series' next air 
date as well as the episode's plot synopsis (if available at the 
time).

The app is currently on the Google Play Store https://play.google.com/store/apps/details?id=com.theupnextapp

## FEATURES:

### Dashboard
- A Dashboard featuring yesterday, today, and tomorrow's shows

### Explore
- An Explore screen featuring Popular, Trending, and Most Anticipated Shows courtesy of Trakt

### Favorites
Now you can add a show as a favorite, which will be synced to your Trakt account. All your favorites will be displayed on the Account screen when your Trakt account is connected. You will require a Trakt.tv account first before you can connect it on Upnext: TV Series Manager.

## Pre-requisites
Please obtain an API key from http://www.omdbapi.com/ before launching the application. You will
need to place this key in gradle.properties as part of the `OMDbKey` property.

```
TraktClientID="[your Trakt Client ID key goes here]"
TraktClientSecret="[your Trakt Client Secret key goes here]"
TraktRedirectURI="[your Trakt redirect URI goes here]"
```

## MAD Score
<img src="https://github.com/akitikkx/upnext/blob/main/screenshots/summary.png" />

## License

MIT License

Copyright (c) 2022 Ahmed Tikiwa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.