# Upnext: TV Series Manager

## Google Developers Dev Library
<b>Upnext: TV Series Manager is now officially part of the <a href="https://devlibrary.withgoogle.com/products/android/repos/akitikkx-upnext" target="_blank">Google Developers Dev Library</a></b>


## Jetpack Compose

<b>NOTE:</b> `Upnext: TV Series Manager` is now using Jetpack Compose! 

<b>All screens are using ComposeView.</b>

Each area of the app is being gradually converted to this modern UI toolkit. 

There are quite a number of changes that need to be done in order to fully migrate `Upnext: TV Series Manager` to Jetpack Compose and, therefore, the migration will be gradual. Should you wish to help with this effort, please see the contributing guidelines and code of conduct.


## What is Upnext: TV Series Manager?
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

<img src="https://github.com/akitikkx/upnext/blob/main/screenshots/web_banner_20_feb_2022.png" />

## Pre-requisites
It is important that you ensure that the below are fully setup or the project will not run.

### Java version
The project is configured to use Java 11. Please ensure that your Android Studio is set to use this and
not the default 1.8 else the project will not build:

```
Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK

```
<img src="https://github.com/akitikkx/upnext/blob/main/screenshots/upnext_java_setup.png" />

### Google Services File

- Ensure that you add your `google-services.json` file to be saved in the app/ directory

### Trakt Setup

- `local.properties` should be used to contain the relevant keys. Note that this file is added in the 
`.gitignore` to ensure this file is never committed to the repository for security reasons as these
  keys should only be known to you, the developer.

- Please obtain an API key from https://trakt.tv/ (directions below) in order for the Trakt functionality to work. You will
need to place this key in `local.properties` (see `local.properties.example` for a sample of how it should be) as follows:

```
TraktClientID="[your Trakt Client ID key goes here]"

TraktClientSecret="[your Trakt Client Secret key goes here]"

TraktRedirectURI="[your Trakt redirect URI goes here]"

In your Trakt account dashboard https://trakt.tv/oauth/applications: 

- Add your new application
- Once created and on the application view in Trakt e.g https://trakt.tv/oauth/applications/<id> you will
  find your Client ID and Client Secret
- When the user navigates to the Account screen while not logged in they will have the option to authorize
  Upnext: TV Series Manager on their Trakt account. Once they click on the "Connect to Trakt" button in the app,
  they will be redirected via Chrome Custom Tabs or Webview using OAuth. A callback URL is required by Trakt for when the
  user has authorized Upnext. If you want to define your own, just make sure that the <data> tag in AndroidManifest.xml
  for MainActivity is updated to reflect your chosen callback URL.
  
  It is currently defined as the following in AndroidManifest.xml:
   - host = 'callback'
   - scheme = 'theupnextapp'
   
  If you want to use the above as the callback, then in Trakt use "theupnextapp://callback" as your callback URL
```

## Architecture

`Upnext: TV Series Manager` is an MVVM app built using Kotlin and the following libraries:

- Jetpack Compose
- ViewModel, LiveData, Kotlin Flow
- Jetpack Navigation
- Jetpack Datastore
- Room
- Hilt
- Data Binding
- Chrome Custom Tabs
- Glide
- Firebase Analytics
- Firebase Crashlytics
- Firebase Core
- Firebase Performance Monitoring
- Jsoup
- Kotlin Coroutines
- AndroidX Lifecycle
- Gson
- Leak Canary
- Flexbox
- Retrofit
- OkHttp
- Moshi

## Code and directory structure
```
> bindings
> common  
> database
> di
> domain
> extensions
> network
> repository
> ui
> work
|_ MainActivity.kt
|_ UpnextApplication.kt

```

## MAD Score
<img src="https://github.com/akitikkx/upnext/blob/main/screenshots/summary.png" />

## Play Store Data Safety
The data safety form has been completed on the Play Store and accepted. Please find the CSV version 
<a href="https://github.com/akitikkx/upnext/blob/main/dataSafety/data_safety_export.csv">here</a>

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
