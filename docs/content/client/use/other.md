---
title: "Other"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T20:46:57+00:00
type: "docs"
draft: false
weight: 390
---

Some other available functionality is described here.


## Show Graph {#show-graph}

Once a composite process or an intermediate composition is created, its graph can be viewed on a separate window.

-   This can be accomplished by **right-clicking** the composition and selecting the {{< icon "client/icons/ShowGraph.png" "show graph icon" "inline" >}} `Show Graph` option.
-   The full graph is also shown when hovering above the blue process box of a (collapsed) composite process.

This can be particularly useful for composite processes which appear as a single atomic process in subsequent compositions.


## Load Compositions {#load-compositions}

In some cases it may be useful to reload the intermediate (binary) composition steps that we followed when a composite process was stored.

-   This can be accomplished by **right-clicking** a composite process and selecting the {{< icon "client/icons/LoadCompositions.png" "load compositions icon" "inline" >}} `Load Compositions` option.

This will create a new workspace and add all the composition steps used to create the selected process as intermediate compositions.

This can be particularly useful for example when a composite process has become invalid due to an updated component and you need to adjust the composition actions, or if a similar copy of the same composition is required, but the workspace no longer exists.


## Inspect &pi;-calculus {#inspect-and-pi-calculus}

The reasoner automatically produces &pi;-calculus specifications of the specified processes. These can be visualized and inspected using the [PiVizTool](http://frapu.de/bpm/piviztool.html), which has been directly integrated with the Client.

{{< tip warning >}}
The PiVizTool relies on [GraphViz](https://graphviz.org/). The `dot` executable must be available in the `PATH` for it to function.
{{< /tip >}}

-   This can be accomplished by **right-clicking** the composition and selecting the {{< icon "client/icons/Inspect.png" "inspect pi calculus icon" "inline" >}} `Inspect pi-calculus` option.

The graph includes a `Request` and a `Response` process, which are responsible for the sending the initial inputs and receiving the final outputs respectively.

The visualization is interactive. Resources are communicated between processes by clicking on black edges or using the icons at the top.

Some familiarity with &pi;-calculus is required to be able to follow the execution steps.

{{< tip warning >}}
The PiVizTool has certain bugs which cause it to fail and give an error despite a valid pi-calculus specification. Unfortunately we have not been able to identify the source or resolve these issues.
{{< /tip >}}