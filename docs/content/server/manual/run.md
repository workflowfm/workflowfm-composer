---
title: "Run"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T13:47:53+00:00
type: "docs"
draft: false
weight: 10230
---

## Configuration {#configuration}

Running the server requires a `.properties` configuration file with 3 elements:

1.  `server`: The host name (default: `localhost`).
2.  `port`: The port to bind to for listening (default: `7000`).
3.  `launchProverCommand`: The location of the [script to run the reasoner]({{< relref "reasoner#script" >}}).

The contents of the `docker.properties` file, which is used in the Docker image, is shown as an example below:

```text
server=localhost
port=7000
launchProverCommand=/server/scripts/launch_prover_docker.sh
```


## Launch {#launch}

To summarize, running the server requires the following:

1.  An installation of the Java server.
2.  A working installation of the reasoner.
3.  A configuration file.

With everything in place, the server can be run from its installation with the following command:

```sh
./bin/WorkflowFM_Server /path/to/configuration/file.properties
```