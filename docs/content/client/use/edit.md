---
title: "Create & Edit"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T20:46:56+00:00
type: "docs"
draft: false
weight: 320
---

-   Clicking on the {{< icon "client/icons/CreateProcess.png" "create process icon" "inline" >}} `Create Process` icon at any time opens a new window for creating a new atomic process.
-   Right-clicking on an atomic process in the `Processes` list on the left and then clicking the {{< icon "client/icons/EditProcess.png" "edit process icon" "inline" >}} `Edit Process` option opens the same window to allow edits.

The available functionality is described below.

Actions can be **undone/redone** at any time using the undo/redo arrows at the top, or `Ctrl-Z` and `Ctrl-Y` respectively.

Click the `Done` button at the bottom when all the desired edits are completed. This will create the new process or update the one being edited.

New and edited atomic processes are added to the `Processes` list on the left.

{{< tip >}}
Editing an atomic process that is used in some composition may render that composition invalid (`unverified`). The composition will need to be [verified again]({{< relref "verify" >}}) with the new atomic specification.
{{< /tip >}}


## Process names {#process-names}

An initial, unique name `P#` is automatically generated for the process.

{{< tip warning >}}
Process names **must** start with a letter and may **only** contain _letters, numbers and underscores_.
{{< /tip >}}

A process can be renamed in 2 ways:

-   By clicking the {{< icon "client/icons/Rename.png" "rename icon" "inline" >}} `Rename` icon.
-   By double clicking the blue box containing the name of the process.

{{< tip >}}
It is standard practice in process modelling to name processes **using verbs** that describe the corresponding action being taken. For example: `CreateAccount`, `GenerateReport`, `CheckOutcome`.
{{< /tip >}}


## Resource names {#resource-names}

Resource names describe the types of input or output resources for a process.

{{< tip warning >}}
Resource names **must** start with a letter and may **only** contain _letters, numbers and underscores_.
{{< /tip >}}

A resource can be renamed by double clicking the **edge** (line) that carries its name.

{{< tip >}}
It is standard practice in process modelling to name resources **using nouns** that describe the corresponding concrete or abstract resource. For example: `CreatedAccount`, `GeneratedReport`, `Payment`.
{{< /tip >}}


## Resource branches {#resource-branches}

Both input and output resources can be added as branches in a tree-like structure. A dialog to fill in the name of the newly added resource is provided each time.

We can specify branches of both parallel (solid lines) and optional (dashed lines) resources.

-   **Double-clicking** on a blue resource circle adds a new resource **at the same level and branch type as the circle**.

    _For example, if a circle belongs to an optional branch (dashed line), double clicking it will produce a new option from the same root as the circle._

-   **Right-clicking** on a blue resource circle reveals the {{< icon "client/icons/Branch.png" "branched arrows icon" "inline" >}} `Add branch` option. Selecting this will add a **new resource branch of the opposite type** to that circle.

    _For example, if a circle belongs to an optional branch (dashed line), the `Add Branch` option will add a new parallel branch starting from that circle and including the original resource (or its children) and the new resource as children._

    This allows us to change the type of a branch and alternate between parallel and optional branching.

-   **Right-clicking** on a blue resource circle also reveals the {{< icon "client/icons/Delete.png" "delete icon" "inline" >}} `Delete branch` option. Selecting this will **delete the resource or the entire branch of resources** belonging to the circle.

    This may lead to branches of the same type collapsing together in a single branch.

    As with all other edit actions, this action can be undone.