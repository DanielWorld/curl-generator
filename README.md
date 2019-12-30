# CurlInterceptor

A cURL interceptor which intercept OkHttp http request and generate cURL

## Gradle build
build.gradle
<pre>
buildscript {
    repositories {
        jcenter()
    }
}

dependencies {
    ...
    // Daniel (2016-06-23 14:37:35): Added CurlInterceptor
    implementation 'com.danielworld:curl-interceptor:2.0.3'
    
    // logger
    implementation 'com.danielworld:logger:1.0.4'
        
    // kotlin
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61"
    
    // annotation
    implementation 'androidx.annotation:annotation:1.1.0'

}
</pre>

```
Copyright (c) 2019 DanielWorld.
@Author Namgyu Park

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
