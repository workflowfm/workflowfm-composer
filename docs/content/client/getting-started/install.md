---
title: "Install"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T20:46:54+00:00
type: "docs"
draft: false
weight: 110
---

To install, you can either grab the pre-built zip file or build the client yourself.


## Release {#release}

Install simply by grabbing the zipped distribution from the [latest release](https://github.com/workflowfm/workflowfm-composer/releases/latest) and unzip to your chosen location.


## Manual build {#manual-build}

The client can be built manually using [Gradle](https://gradle.org/), which ships with the repository.

You can use either:

-   Pre-installed Gradle: `gradle`
-   Linux: `./gradlew`
-   Windows: `gradlew.bat`

{{< tip >}}
Building requires **JDK 11** or above.
{{< /tip >}}

The client can be compiled and packaged with a single command:

```sh
gradle :client:distZip
```

This will create the file `client/build/distributions/WorkflowFM_Composer-{VERSION}.zip`.

Unzip it to your favourite location to obtained a client installation.