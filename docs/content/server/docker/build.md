---
title: "Build Image"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T13:47:50+00:00
type: "docs"
draft: false
weight: 10120
---

You can build the Docker image yourself from source.

First, clone the repository:

```sh
git clone https://github.com/workflowfm/workflowfm-composer.git
```

Make sure to _recursively_ populate submodules:

```sh
cd workflowfm-composer/
git submodule update --init --recursive --depth 1
```

Then build the image with the provided `Dockerfile`:

```sh
docker build -t composer-server .
```

Then run a container using:

```sh
docker run -p 7000:7000 --name workflowfm-server --detach composer-server
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