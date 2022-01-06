---
title: "Compose"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-06T19:51:31+00:00
type: "docs"
draft: false
weight: 330
---

Composition involves the combination of 2 processes (_binary compositions_) in a single composite process.

-   This requires an active `Workspace`. A new workspace can be created using the {{< icon "client/icons/CreateWorkspace.png" "create workspace icon" "inline" >}} `Create Workspace` icon.
-   Processes can be added to the workspace by **double-clicking** them in the `Processes` list on the left or by **right-clicking** them and selecting the {{< icon "client/icons/Add.png" "add icon" "inline" >}} `Add Graph` option.

Once added, processes can be composed together in 3 ways:

1.  **In sequence**
2.  **In parallel**
3.  **Conditionally**

All composition actions are performed in **\*2 steps**: **first select (left-click)** on an element **and then right-click** on a target element to compose them together. The selection is different for each action, as described below.

Each time a composition action is performed successfully, a new _intermediate_ composition is created and listed in the `Compositions` list on the left.

{{< tip >}}
Each workspace has its own `Compositions` list. Make sure you have an active workspace to view its list. You may need to drag and resize the list using the bar at the bottom of the `Processes` list to make it visible.
{{< /tip >}}

Intermediate compositions are named automatically as `_Step#` using a unique number (With separate counting for each workspace). These can be used as components in further composition actions. They cannot be renamed, but they can be [stored as new processes]({{< relref "verify" >}}).

In some cases and due to certain design decisions (some of which are mentioned below), the result of a composition action may seem bizarre or unexpected. However, every composition action is performed using **formal, logic-based reasoning** and is guaranteed to be correct.

For example, the reasoner guarantees systematic resource accounting, so that unused resources (for example in sequences of optional processes) may appear as new outputs.


## In Sequence {#in-sequence}

Composing 2 processes in sequence creates a composition where the output of one process connects to an input of the same type of another process. This is the most common composition action.

-   This can be accomplished by seleting a input/output resource and then right-clicking on a corresponding output/input resource of another process.

The UI helps identify valid targets for sequential composition:

-   **Hovering** above a resource highlights matching targets using **orange boxes**.
-   **Selecting** a resource highlights matching targets using **dark green boxes** until the resource is de-selected.

{{< tip >}}
Sequential composition attempts to work **maximally** and connect together as many of the matching resources as possible.
{{< /tip >}}

For example, consider a process with parallel outputs `A` and `B`, and another process with corresponding parallel inputs `A` and `B`. We can compose them in sequence by selecting the output `A` of the first process and right-clicking on the input `A` of the other. Even though we selected `A` for composition, both `A` and `B` will be connected in the resulting composite process.

The rationale for this design choice is beyond the scope of this documentation, but we refer the interested reader to the [relevant publication](https://link.springer.com/chapter/10.1007/978-3-030-13838-7%5F5).

An example sequential composition, with the orange highlight boxes, is shown below:

{{< picture "client/Sequence.png" "client/Sequence.png" "an example sequential composition" >}}


## In Parallel {#in-parallel}

Composing 2 processes in parallel groups them together in a composite process that executes them both at the same time. The resulting composition has all of the inputs and all of the outputs from both processes.

-   This can be accomplished by selecting a blue process box and then right-clicking on a different blue process box.

A triangle `join` node will appear in the composite process in this case.


## Conditionally {#conditionally}

The conditional composition of 2 processes leads to a composite process where only one of the 2 processes will be executed depending on runtime conditions. This type of composition is useful in cases where each of the components of an optional output of a process needs to be handled by a different receiving process.

Which process is executed is dictated by a **new optional input**, using one input from each process. If the first option is provided (at runtime), the first process will be executed, whereas if the second option is provided, the other process will be executed.

-   This can be accomplished by seleting an input resource and then right-clicking on an input resource of another process.

At least one diamond `with` node will appear in the composite process in this case.