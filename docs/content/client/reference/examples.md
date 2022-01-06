---
title: "Examples"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-06T19:51:35+00:00
type: "docs"
draft: false
weight: 410
---

A list of examples is available in the default installation of the client. You can inspect them by loading the corresponding file, verifying all processes and compositions and viewing their graphs.

The following examples are included:

1.  `SimpleCopyAndSequence.json`: Simple example demonstrating the copy node and 2 separate methods of serial sequential composition. Inspection of the graph and underlying &pi;-calculus structure shows the difference between composing right-to-left (simpler structure) and left-to-right (introduces axiom buffers).

2.  `SimpleOptionalTreatment.json`: Simple healthcare-inspired example demonstrating resource accounting when handling optional/exceptional outcomes as presented in:
    -   _[A Pragmatic, Scalable Approach to Correct-by-construction Process Composition Using Classical Linear Logic Inference](https://link.springer.com/chapter/10.1007/978-3-030-13838-7%5F5)_

3.  `BuySki.json`: Example of a workflow for buying Ski equipment as presented in:
    -   _[A theorem proving framework for the formal verification of Web Services Composition](https://arxiv.org/abs/1108.2348)_

4.  `HomePurchase.json`: Example of a workflow for purchasing property as presented in:
    -   _[Formal verification of Web Services composition using linear logic and the pi-calculus](https://ieeexplore.ieee.org/document/6061099)_

5.  `HealthcareHandover.json`: Example of 2 patient handover workflows via assignment and delegation, as presented in:
    -   _[Formal verification of collaboration patterns in healthcare](https://www.tandfonline.com/doi/abs/10.1080/0144929X.2013.824506)_
    -   _[Rigorous process-based modelling of patterns for collaborative work in healthcare teams](https://ieeexplore.ieee.org/document/6266330)_