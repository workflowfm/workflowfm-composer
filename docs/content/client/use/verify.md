---
title: "Store & Verify"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-05T20:46:56+00:00
type: "docs"
draft: false
weight: 340
---

## Store {#store}

**Storing** an intermediate composition allows the creation of a new, composite process that can be reused in the same way as atomic processes.

The expectation here is that the user composes processes together using composition actions and generating intermediate compositions. Once they are happy with a particular composition and they want to keep it for further use as a new process, they can store it.

-   This can be accomplished by right clicking on an intermediate composition of a workspace in the `Compositions` list on the left. Then select the option {{< icon "client/icons/Composite.png" "composite process icon" "inline" >}} `Store Composition`.

A new window will be opened, showing the graph of the selected composition:

{{< picture "client/Store.png" "client/Store.png" "the dialog window for storing an intermediate composition" >}}

A new, unique name must be provided for the composite process, following [the same rules and practices as for atomic process naming]({{< relref "edit#process-names" >}}).

Clicking on the `Done` button at the bottom of the window completes the storage process. The new composite process should appear in the `Processes` list on the left.


## Verify {#verify}

**Verifying** a process involves sending its specification to the reasoner and verifying its correctness.

There are several options to verify different processes:

-   The {{< icon "client/icons/VerifyAll.png" "verify all icon" "inline" >}} `Verify All` icon attempts to verify all atomic and composite processes. This can be particularly useful in a newly opened file.
-   The {{< icon "client/icons/VerifyIntermediates.png" "verify intermediates icon" "inline" >}} `Verify All Intermediates` icon attempts to verify all intermediate processes in an active workspace.
-   Right-clicking an atomic or composite process and selecting the {{< icon "client/icons/Verify.png" "verify icon" "inline" >}} `Verify Process` option verifies the selected process only.
-   Right-clicking an intermediate composition and selecting the {{< icon "client/icons/Verify.png" "verify icon" "inline" >}} `Verify Composition` option verifies the selected composition only.
-   Right-clicking an atomic or composite process or an intermediate composition and selecting the {{< icon "client/icons/VerifyParents.png" "verify process&parents icon" "inline" >}} `Verify Process&Parents` option verifies the all of the components of the selected process recursively and then the process itself.

These options allow a step-by-step verification process in case the exact source of an error needs to be identified.

Verification may be required in several situations where a process specification may have changed either directly or indirectly because of another process. Typical examples include:

-   Loading a saved file.
-   Editing or deleting a component atomic process.
-   Replacing/updating a component atomic or composite process.

If there is uncertainty about the correctness of a process, it is marked as `unchecked` or `unverified`. This is indicated by a yellow warning icon as shown below.

If verification of a particular composition fails (for instance because a composition action is no longer possible), the corresponding reasoner error will be displayed and the process will be marked as `invalid` with a red icon as shown below.

| Icon                                                                                                   | Decription                         |
|--------------------------------------------------------------------------------------------------------|------------------------------------|
| {{< icon "client/icons/ProcessWarning.png" "unchecked process icon" "inline" >}}                       | Unchecked atomic process           |
| {{< icon "client/icons/CompositeWarning.png" "unchecked composite process icon" "inline" >}}           | Unchecked composite process        |
| {{< icon "client/icons/IntermediateWarning.png" "unchecked intermediate composition icon" "inline" >}} | Unchecked intermediate composition |
| {{< icon "client/icons/ProcessInvalid.png" "invalid process icon" "inline" >}}                         | Invalid atomic process             |
| {{< icon "client/icons/CompositeInvalid.png" "invalid composite process icon" "inline" >}}             | Invalid composite process          |
| {{< icon "client/icons/IntermediateInvalid.png" "invalid intermediate compositionicon" "inline" >}}    | Invalid intermediate composition   |