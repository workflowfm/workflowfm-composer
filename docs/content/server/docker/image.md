---
title: "Quick Setup"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-07T12:04:56+00:00
type: "docs"
draft: false
weight: 10110
---

The easiest setup of the server is using the latest available [Docker image](https://github.com/workflowfm/workflowfm-composer/pkgs/container/composer-server).

Pull the image using:

```sh
docker pull ghcr.io/workflowfm/composer-server:latest
```

Then run a container using:

```sh
docker run -p 7000:7000 --name workflowfm-server --detach ghcr.io/workflowfm/composer-server:latest
```

-   The name `workflowfm-server` is optional and can be changed to whatever you want your server container to be named.
-   The port can also be bound to a different system port, e.g. using `-p 9000:7000` to bind it to port `9000`.

{{< tip >}}
The container may take a few minutes to fully start!
{{< /tip >}}

This is because it loads [HOL Light](https://github.com/workflowfm/hol-light) and the [WorkflowFM Reasoner](https://github.com/workflowfm/workflowfm-reasoner) from scratch. You can follow progress using:

```sh
docker logs --follow workflowfm-server
```