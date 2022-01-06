---
title: "Configuration"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-06T19:51:36+00:00
type: "docs"
draft: false
weight: 490
---

Configuration values are stored in a `.workflowfm` directory at the user's home directory, defaulting in the `composer.properties` file.

It is **not** necessary to explicitly set any of these configuration parameters, but it is possible to tweak the behaviour of the client in some ways if desired.

-   You can manually set parameters in the configuration file following the standard [.properties file format](https://en.wikipedia.org/wiki/.properties), `variable = value`.
-   You can also create a custom property file to be used instead of the default file. Simply pass the file path as an argument when starting the client.

Some values may be changed through the use of the interface and will be overwritten in the file. These are marked in the _Auto_ column below.

| Parameter name            | Type                   | Description                                                                                          | Default                  | Auto                                             |
|---------------------------|------------------------|------------------------------------------------------------------------------------------------------|--------------------------|--------------------------------------------------|
| `processNodeWidth`        | Integer                | The pixel width of the process vertex.                                                               | 120                      | \*                                               |
| `processNodeHeight`       | Integer                | The pixel height of the process vertex.                                                              | 40                       |                                                  |
| `processNodeAutoResize`   | Boolean                | If `true` the width of process vertices is resized to fit their label.                               | true                     |                                                  |
| `processCopierNodeRadius` | Integer                | The pixel radius of the copier node vertex.                                                          | 15                       |                                                  |
| `interHierarcySpacing`    | Integer                | The vertical distance between 2 separate process graphs.                                             | 40                       |                                                  |
| `interRankCellSpacing`    | Integer                | The horizontal distance between 2 vertices of the same process graph.                                | 110                      |                                                  |
| `atomicProcessColour`     | String (hex colour)    | The colour of an atomic process vertex.                                                              | #BBDEFB                  |                                                  |
| `compositeProcessColour`  | String (hex colour)    | The colour of a composite process vertex.                                                            | #64B5F6                  |                                                  |
| `portEdgeColour`          | String (hex colour)    | The colour of a resource vertex.                                                                     | `atomicProcessColour`    |                                                  |
| `edgeColour`              | String (hex colour)    | The colour of a solid edge.                                                                          | #686868                  |                                                  |
| `bufferColour`            | String (hex colour)    | The colour of a buffer edge\*\*.                                                                     | #686868                  |                                                  |
| `hoverHighlightColour`    | String (hex colour)    | The colour used to highlight matching resource vertices when hovering above one.                     | #F78400                  |                                                  |
| `selectHighlightColour`   | String (hex colour)    | The colour used to highlight selected vertices.                                                      | #33691E                  |                                                  |
| `proofScriptDirectory`    | String (directory)     | The default directory for opening files.                                                             | `./proofs/`              | {{< icon "checkmark.png" "a green checkmark" >}} |
| `imageDirectory`          | String (directory)     | The default directory for storing screenshots.                                                       | `proofScriptDirectory`   | {{< icon "checkmark.png" "a green checkmark" >}} |
| `frameWidth`              | Integer                | The width of the UI window. (Attempts to resize to that on start.)                                   | 1000                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `frameHeight`             | Integer                | The height of the UI window. (Attempts to resize to that on start.)                                  | 1000                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `deployStateful`          | Boolean                | The default value of the corresponding tick box in the [deployment dialog]({{< relref "deploy" >}}). | true                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `deployMain`              | Boolean                | The default value of the corresponding tick box in the [deployment dialog]({{< relref "deploy" >}}). | true                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `projectName`             | String                 | The last used project name in the [deployment dialog]({{< relref "deploy" >}}).                      |                          | {{< icon "checkmark.png" "a green checkmark" >}} |
| `deployPackageName`       | String (Scala package) | The last used package name in the [deployment dialog]({{< relref "deploy" >}}).                      | `com.workflowfm.project` | {{< icon "checkmark.png" "a green checkmark" >}} |
| `deployFolder`            | String (directory)     | The last used target directory in the [deployment dialog]({{< relref "deploy" >}}).                  | `./`                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `server`                  | String (host/IP)       | The last used server host/IP.                                                                        | `localhost`              | {{< icon "checkmark.png" "a green checkmark" >}} |
| `port`                    | Integer (port)         | The last used server port.                                                                           | 7000                     | {{< icon "checkmark.png" "a green checkmark" >}} |
| `serverMaxAttempts`       | Integer                | The maximum number of attempts to reconnect to the server upon failure.                              | 10                       |                                                  |

-   \* The value `processNodeWidth` is unimportant if `processNodeAutoResize` is `true`.
-   \*\* _Buffer edges_ were originally used to represent resources that are not directly connected to a process, but go through the so-called _axiom buffers_. In more recent versions of the system, we considered this information more confusing than helpful, so we use the same colour for those edges. Moreover, the algorithm that determines which resources are buffered is not always accurate. Because of this, the value `bufferColour` will be deprecated in future versions.
    The value of `bufferColour`  should normally be the set to be the same as `edgeColour`.