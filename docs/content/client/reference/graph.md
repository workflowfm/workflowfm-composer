---
title: "Graph elements"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-06T19:51:35+00:00
type: "docs"
draft: false
weight: 430
---

The following is a complete list of the visual elements, including nodes (vertices) and edges, that can be encountered in a process graph.


## Vertices {#vertices}

| Name              | Visual                                                                                                                            | Description                                                                                                                                             |
|-------------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| Resource          | {{< picture "client/graph/Resource.png" "client/graph/Resource.png" "a small light blue circle" >}}                               | An input or output resource.                                                                                                                            |
| Atomic Process    | {{< picture "client/graph/Process.png" "client/graph/Process.png" "a light blue, rounded rectangle" >}}                           | An atomic process.                                                                                                                                      |
| Composite Process | {{< picture "client/graph/Composite.png" "client/graph/Composite.png" "a dark blue, rounded rectangle" >}}                        | A composite process.                                                                                                                                    |
| Copy node         | {{< picture "client/graph/Copy.png" "client/graph/Copy.png" "a large, light blue circle with a black circled X in the middle" >}} | A process with a single input and multiple parallel outputs of the same type is assumed to be a _copy_ process that makes multiple copies of its input. |
| Join node         | {{< picture "client/graph/Join.png" "client/graph/Join.png" "a light blue triangle pointing right" >}}                            | A special node used when the output of a composite process does not have a single or clear source.                                                      |
| Merge node        | {{< picture "client/graph/With.png" "client/graph/With.png" "a light blue diamond with a black ampersand in the middle" >}}       | A special node used when an _optional_ input/output combines resources from multiple component processes.                                               |


## Edges {#edges}

Edges can be solid or dashed as shown below:

{{< picture "client/graph/Edges.png" "client/graph/Edges.png" "one solid and one dashed grey arrow pointing to the right" >}}

Dashed edges are used to indicate that the corresponding resource (or group of resources) is _optional_, i.e. part of an optional tree of resources where only one of the branches can be provided.