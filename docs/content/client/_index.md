---
title: "Client"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-06T19:51:27+00:00
type: "docs"
draft: false
menu:
  main:
    identifier: "client"
    weight: 100
---

Welcome to the **WorkflowFM Composer Client documentation**.

The Client provides a Java-based Graphical User Interface or formally verified process composition using the [WorkflowFM Reasoner](http://docs.workflowfm.com/workflowfm-reasoner/).

{{< tip >}}
The Client requires a running instance of the [Server](../server).
{{< /tip >}}

It allows the specification of processes using their input and output resources. Processes can then be composed together with simple mouse gestures. Every composition action is sent to the reasoner, via the server, and then performed using logic-based reasoning so that the result is formally verified.

{{< tip >}}
Configuration values are stored in the user's home directory, under `.workflowfm/`.
{{< /tip >}}

{{< button "./getting-started/" "Get started" >}}