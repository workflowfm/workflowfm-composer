---
title: "Server"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T13:47:51+00:00
type: "docs"
draft: false
weight: 10210
---

The server can be built using [Gradle](https://gradle.org/), which ships with the repository.

You can use either:

-   Pre-installed Gradle: `gradle`
-   Linux: `./gradlew`
-   Windows: `gradlew.bat`

{{< tip >}}
Building requires **JDK 11** or above.
{{< /tip >}}

The server can be compiled and packaged with a single command:

```sh
gradle :server:distZip
```

This will create the file `server/build/distributions/WorkflowFM_Server-{VERSION}.zip`.

Unzip it to your favourite location to obtained a server installation.