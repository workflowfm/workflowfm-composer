---
title: "Getting Started"
author: ["Petros Papapanagiotou"]
lastmod: 2021-10-18T11:35:50+01:00
type: "docs"
draft: false
weight: 101
---

The WorkflowFM Composer client is built using Java 11 and [Gradle](https://gradle.org/).

It requires a running instance of the [server](../../server), with an accessible host and port.

Install simply by grabbing the zipped distribution from the [latest release](https://github.com/workflowfm/workflowfm-composer/releases/latest) and unzip to your chosen location.

Start the Client using (Linux/Mac):

```sh
./bin/WorkflowFM_Composer
```

or similarly in Windows:

```nil
.\bin\WorkflowFM_Composer.bat
```

User preferences are stored in the user's home directory, under `.workflowfm/`.

Once loaded, enter the address and port of the server in the dialog provided:

{{< picture "client/ConnectDialog.png" "client/ConnectDialog.png" "dialog titled Connect to Reasoner with input fields for a host and port" >}}
