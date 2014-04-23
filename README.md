# MoPub Android SDK

Thanks for taking a look at MoPub! We take pride in having an easy-to-use, flexible monetization solution that works across multiple platforms.

Sign up for an account at [http://app.mopub.com/](http://app.mopub.com/).

Help is available on the [wiki](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started).

## Download

The MoPub SDK is distributed as source code that you can include in your application.  MoPub provides two prepackaged archives of source code:

- **[MoPub Android Full SDK.zip](http://bit.ly/YUdU9v)**

  Includes everything you need to serve HTML and MRAID MoPub advertisiments *and* built-in support for two major third party ad networks - [Google AdMob](http://www.google.com/ads/admob/) and [Millennial Media](http://www.millennialmedia.com/) - including the required third party binaries.

- **[MoPub Android Base SDK.zip](http://bit.ly/YUdWhH)**

  Includes everything you need to serve HTML and MRAID MoPub advertisements.  No third party ad networks are included.

## Integrate

Integration instructions are available on the [wiki](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started).


## New in this Version

Please view the [changelog](https://github.com/mopub/mopub-android-sdk/blob/master/CHANGELOG.md) for details.

  - **Native Ads** public release; integration instructions and documentation available on the [GitHub wiki](https://github.com/mopub/mopub-android-sdk/wiki/Native-Ads-Integration)
  - Changed minimum supported Android version to Froyo (Android 2.2, API level 8)
  - Added support for Google Play Services advertising identifier
  - Renamed the `com.mopub.mobileads.MraidBrowser` Activity to `com.mopub.common.MoPubBrowser`.
       - **Important Note:** This change requires a modification to the `AndroidManifest`. The updated set of requisite activity permissions are as follows:
      
      	```      	      	
    <activity android:name="com.mopub.common.MoPubBrowser"
				android:configChanges="keyboardHidden|orientation"/>
    <activity android:name="com.mopub.mobileads.MoPubActivity"
            	android:configChanges="keyboardHidden|orientation"/>
    <activity android:name="com.mopub.mobileads.MraidActivity"
            	android:configChanges="keyboardHidden|orientation"/>
	<activity android:name="com.mopub.mobileads.MraidVideoPlayerActivity"
            	android:configChanges="keyboardHidden|orientation"/>
		```  
  - Upgraded the bundled `android-support-v4` library to r19.1.
      - **Note for Maven users:** Newer versions of the `android-support-v4` artifact are unavailable on Maven central, so we have included a small script to update the version in your local artifact repository. Please navigate to the `mopub-sdk` directory, and run `scripts/mavenize_support_library`.

## Requirements

Android 2.2 and up

## License

The MoPub Android SDK is open sourced under the New BSD license:

Copyright (c) 2010-2013 MoPub Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of MoPub nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
