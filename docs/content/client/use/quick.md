---
title: "Quick Start"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T20:46:55+00:00
type: "docs"
draft: false
weight: 310
---

A quick overview of some of the features is described in the following tasks:


## Create {#create}

-   Click on the {{< icon "client/icons/CreateProcess.png" "create process icon" "inline" >}} `Create Process` icon.
-   Double click on the _process box_ labelled `P1` to **rename the process**.
-   Double click on one of the _edges_ (initially labelled `X`) to **rename the corresponding resource**.
-   Double click on one of the small, _resource circles_ to **add a new branch**.
-   Right-click on one of the small, _resource circles_ to **add a branch of different type**  (from parallel to optional and vice-versa) or to **delete a branch**.


## Compose {#compose}

-   Open a **new workspace** with the {{< icon "client/icons/CreateWorkspace.png" "create workspace icon" "inline" >}} `Create Workspace` icon.
-   Double click on a _process_ in the `Processes` list on the left to **add its graph to the workspace**.
-   Compose processes **in sequence** by selecting (clicking) an _output resource_ (edge or circle) and then **right-clicking** an _input resource_ with the same name belonging to a different process.
-   Compose processes **in parallel** by selecting (clicking) a _process box_ then **right-clicking** _another process box_.
-   Compose processes **conditionally** by selecting (clicking) an _input resource_ (edge or circle) and then **right-clicking** another _input resource_ belonging to different process.
-   Right click on a _process box_ and select {{< icon "client/icons/Composite.png" "composite process icon" "inline" >}} `Store Composition` to collapse and \*store a composite proces\*s as a reusable composite component under a new name.


## Edit &amp; Verify {#edit-and-verify}

-   Right click on a _process_ in the `Processes` list on the left.
-   Select {{< icon "client/icons/EditProcess.png" "edit process icon" "inline" >}} `Edit Process` to **change the specification** of an _atomic_ process.
-   Select {{< icon "client/icons/Delete.png" "delete icon" "inline" >}} `Delete Process` to delete a process.
-   Changing or deleting a process specification affects all composite processes that depend on it. These become **unverified**: {{< icon "client/icons/CompositeWarning.png" "composite process warning icon" "inline" >}}.
-   Right click on an _unverified process_ and select {{< icon "client/icons/VerifyParents.png" "verify process&parents icon" "inline" >}} `Verify Process&Parents` to **rerun and verify the composition**.