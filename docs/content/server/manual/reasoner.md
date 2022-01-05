---
title: "Reasoner"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T12:26:57+00:00
type: "docs"
draft: false
weight: 220
---

## Installation {#installation}

The [WorkflowFM Reasoner](https://github.com/workflowfm/workflowfm-reasoner) can be installed following the [instructions in its documentation](http://docs.workflowfm.com/workflowfm-reasoner/docs/install/).

The involved `hol-light` repository can already be found as a submodule under `server/hol-light`.

Using [checkpointing](http://docs.workflowfm.com/workflowfm-reasoner/docs/install/hol-light/#checkpointing), if possible, can help build a server that runs _instantly_.


## Launch script {#script}

Once the reasoner is installed and working, we need to build a script that runs the reasoner and pipes the input/output to the server.

The `scripts/` directory contains bash scripts that can help achieve this. Both scripts assume that the reasoner is installed in a `hol-light/` directory within the server installation. If that is not the case, this can be adjusted using a symbolic link:

```sh
ln -s full/path/to/reasoner hol-light
```

-   If checkpointing (dmtcp) was used, the reasoner can run using `./scripts/launch_prover_local.sh`.
-   If checkpointing is **not** available, you can use `./scripts/launch_prover_docker.sh` which is what the Docker build uses to load HOL Light and the reasoner from scratch.
-   You can also run the reasoner on a remote machine using an ssh tunnel. This assumes an installation of the server _and_ the reasoner on that machine, the use of checkpointing, and password-less ssh access to the remote machine. In that case you can use `./scrips/setup_remote_prover.sh` to create a remote launch script (`./scripts/launch_prover.sh`), using the url of the remote machine and the absolute path to the installation of the reasoner:

    ```sh
    ./scripts/setup_remote_prover.sh someuser@host.of.reasoner.com /home/someuser/workflowfm-server/scripts/launch_prover_local.sh
    ```

    Make sure you run the corresponding script and verify it does load the reasoner correctly and without errors. The reasoner should run through the script and allow you to issue OCaml toplevel commands. It should react (and exit) if you provide the following command:

    ```ocaml
    exit(0);;
    ```