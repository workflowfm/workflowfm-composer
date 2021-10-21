---
title: "WorkflowFM Composer"
author: ["Petros Papapanagiotou"]
draft: false
---

[![img](https://img.shields.io/badge/version-{{< version >}}-brightgreen.svg)](https://github.com/workflowfm/workflowfm-reasoner/releases/latest) [![img](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg)](https://opensource.org/licenses/Apache-2.0)

{{< tip >}}
A diagrammatic tool for formally verified process modelling and composition.
{{< /tip >}}

{{< button "client/" "Client Docs" >}}{{< button "server/" "Server Docs" >}}{{< button "https://github.com/workflowfm/workflowfm-composer" "Source" >}}


## About

{{< tip warning >}}
*Docs are under construction...*
{{< /tip >}}

The WorkflowFM Composer consists of a Java-based server and GUI for formally verified process composition using the [WorkflowFM Reasoner](http://docs.workflowfm.com/workflowfm-reasoner/). It provides a visual, diagrammatic interface to specify processes based on their input and output resources, and compose them together to form more complex workflows. 

Actions in the GUI of the [Client](client/) are sent to the [Server](server/), which in turn is able to interact directly with the [Reasoner](http://docs.workflowfm.com/workflowfm-reasoner/). The Reasoner then performs the necessary logic-based inference within the rigorous environment of the HOL Light theorem prover. 

The end result is a composite process that is provably correct and has the following properties:
- **Systematic resource accounting**: No resources appear out of nowhere or disappear into thin air.
- **Deadlock and livelock freedom**: The constructed workflows can be executed without fear for deadlocks or livelocks.
- **Type checked composition**: The correctness of the types of all connected resources is ensured via the logical proof.
- **Fully asynchronous and concurrent execution**: During workflow execution, each component process can be executed fully asynchronously (see the [PEW engine](http://docs.workflowfm.com/pew) for more details) and concurrently, without introducing any conflicts, race conditions, or deadlocks.


### Key Features

- Visual specification of processes based on ther input and output resources.
- Intuitive gestures for process composition.
- Formally verified composite processes based on rigorous logic-based reasoning.
- Simplified diagrammatic representation of complex logic-based deduction.
- Automated tracking of unused resources in compositions.
- Highlighting of matching resources.
- Workspaces for the development of multiple compositions at the same time.
- Workflow visuazliation using the [PiVizTool](http://frapu.de/bpm/piviztool.html).
- Export Scala code to execute workflows using the [PEW engine](http://docs.workflowfm.com/pew).
- Export of PNG images.


<a id="authors"></a>

## Authors


### Maintainer

[Petros Papapanagiotou](https://github.com/PetrosPapapa) - [{{< image "images/web.svg" "Home page" "icon nomargin">}}](https://homepages.inf.ed.ac.uk/ppapapan/) - [{{< image "images/email.svg" "Email" "icon nomargin">}}](mailto:petros@workflowfm.com?subject=WorkflowFM%20Reasoner) - [{{< image "images/twitter.svg" "Twitter" "icon nomargin">}}](https://twitter.com/petrospapapa)


### Contributors

A big thank you to the following contributors in order of appearance:


- Jacques Fleuriot - [{{< image "images/web.svg" "Home page" "icon nomargin">}}](https://homepages.inf.ed.ac.uk/jdf/)
- Sean Wilson - [{{< image "images/web.svg" "Home page" "icon nomargin">}}](https://www.seanw.org)
- [James Vaughan](https://github.com/JeVaughan)
- [Filip Smola](https://github.com/pilif0)



### Groups & Organizations


<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">
<tbody>
<tr>
<td class="org-left"><a href="https://aiml.inf.ed.ac.uk/">Artificial Intelligence Modelling Lab</a></td>
</tr>


<tr>
<td class="org-left"><a href="https://web.inf.ed.ac.uk/aiai">Artificial Intelligence and its Applications Institute</a></td>
</tr>


<tr>
<td class="org-left"><a href="https://www.ed.ac.uk/informatics/">School of Informatics, University of Edinburgh</a></td>
</tr>
</tbody>
</table>


<a id="references"></a>

## References

Please cite the following publication in reference to this project:

-   P. Papapanagiotou, J. Fleuriot. [WorkflowFM: A Logic-Based Framework for Formal Process Specification and Composition](https://link.springer.com/chapter/10.1007/978-3-319-63046-5%5F22). CADE, 2017.

Sample of other relevant references:

- P. Papapanagoiotou, J. Fleuriot, S. Wilson. [Diagrammatically-driven formal verification of Web-Services composition](https://link.springer.com/chapter/10.1007/978-3-642-31223-6_25). Diagrams, 2012.
- P. Papapanagiotou. [A formal verification approach to process modelling and composition](https://era.ed.ac.uk/handle/1842/17863). PhD Thesis, 2014.


## License

Distributed under the Apache 2.0 license. See [LICENSE](https://github.com/workflowfm/workflowfm-composer/blob/master/LICENSE) for more information.

Copyright &copy; 2009-2021 [The University of Edinburgh](https://www.ed.ac.uk/) and [contributors](#authors)
