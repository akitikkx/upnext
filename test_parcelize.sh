#!/bin/bash
./gradlew :core:domain:compileDebugKotlin --info | grep "parcelize"
