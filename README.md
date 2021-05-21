# StatusHandler

[![Maven Central](https://img.shields.io/maven-central/v/by.shostko/status-handler?style=flat)](#integration) [![API-level](https://img.shields.io/badge/API-14+-blue?style=flat&logo=android)](https://source.android.com/setup/start/build-numbers) [![License](https://img.shields.io/badge/license-Apach%202.0-green?style=flat)](#license) 

## Integration

The library is now available in Maven Central repository:
```gradle
dependencies {
    implementation 'by.shostko:status-handler:0.+'
}
```

For additional support of easy creation DataSources for paging, ViewModels and Workers add any of these:
```gradle
dependencies {
    implementation 'by.shostko:status-handler-paging:0.+'
    implementation 'by.shostko:status-handler-viewmodel:0.+'
    implementation 'by.shostko:status-handler-worker:0.+'
}
```

Also don't forget to additional mandatory dependencies:
```gradle
dependencies {
    // for base module
    implementation 'io.reactivex.rxjava2:rxjava:2.+' 
    
    // for paging module
    implementation 'androidx.paging:paging-runtime:2.+'
    
    // for viewmodel module
    implementation 'androidx.recyclerview:recyclerview:1.+'
    implementation 'io.reactivex.rxjava2:rxjava:2.+'
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.+''
    annotationProcessor 'androidx.lifecycle:lifecycle-compiler:2.+'' // use kapt for kotlin
    
    // for worker module
    implementation 'androidx.work:work-runtime-ktx:2.+'
    implementation 'androidx.work:work-rxjava2:2.+'
}
```

### License

Released under the [Apache 2.0 license](LICENSE).

```
Copyright 2019 Sergey Shostko

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
