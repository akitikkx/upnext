/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.extensions

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "custom", device = "spec:width=1280dp,height=800dp,dpi=480", showBackground = true)
@Preview("desktop", device = "id:desktop_medium", showBackground = true)
annotation class ReferenceDevices