---
title: "Server"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T12:26:54+00:00
type: "docs"
draft: false
menu:
  main:
    identifier: "server"
    weight: 100
---

Welcome to the **WorkflowFM Composer Server documentation**.

The Server provides an intermediate layer between the [WorkflowFM Reasoner](http://docs.workflowfm.com/workflowfm-reasoner/) and the Client. It exposes a raw socket to allow the client to communicate remotely with the reasoner.

{{< tip warning >}}
The server is currently insecure for public use. Avoid deploying it in a public server or outside a firewall.
{{< /tip >}}

The reasoner is based on [HOL Light](https://github.com/jrh13/hol-light), which runs on the OCaml toplevel. This makes it difficult to manage in a scalable way and to connect to it remotely or through the web. It also limits its usage to a single user. The server addresses these issues by managing connections through a raw socket and queueing requests to the reasoner.

The server can be deployed either manually or through Docker. Using the [existing Docker image](https://github.com/workflowfm/workflowfm-composer/pkgs/container/composer-server) makes things much easier, but this documentation covers a few different ways to build and deploy it.

Once the server is deployed, you can run an connect multiple [clients](../client) to it.

{{< button "./docker/image/" "Get started" >}}