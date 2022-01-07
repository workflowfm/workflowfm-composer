---
title: "Deploy"
author: ["Petros Papapanagiotou"]
lastmod: 2022-01-07T12:07:08+00:00
type: "docs"
draft: false
weight: 350
---

The reasoner can automatically generate executable [Scala](https://www.scala-lang.org/) code for process compositions, including code templates for the involved resource types and atomic processes.

The code relies on the use of the [WorkflowFM PEW execution engine](https://github.com/workflowfm/pew).

-   This can be accomplished  by **right-clicking** on a composite process and selecting the {{< icon "client/icons/Deploy.png" "deploy icon" "inline" >}} `Deploy in Scala` option.

This will open a new window with the appropriate dialog for deploying code. Using the [Ski example]({{< relref "examples" >}}), the window is shown below:

{{< picture "client/SkiDeploy.png" "client/SkiDeploy.png" "the Scala deployment dialog with details for the Ski example" >}}


## Roadmap {#roadmap}

The reasoner is able to automatically generate workflow code that corresponds to the verified composition.

The roadmap of that process is as follows:

{{< picture "client/DeployRoadmap.png" "client/DeployRoadmap.png" "a diagram of the described deployment roadmap" >}}

Composing atomic processes in the WorkflowFM reasoner results in a correct-by-construction &pi;-calculus specification. This essentially describes the appropriate connections between the component processes so that they are executed in the right order and in an asynchronous way. The &pi;-calculus specification of a composition can be automatically translated in Scala code for the [PEW engine](https://github.com/workflowfm/pew).

Atomic components are originally defined in an abstract way by the user. The code generated for them consists of an abstract trait with the appropriate function type, such that fits the formal input and output specification. The user should then provide concrete implementations for those traits to complete the deployment.

In addition, the resource types are also introduced in an abstract way. The user is required to instantiate those abstract types with concrete Scala types.

In summary, here are the necessary steps to complete a deployment:

1.  Set up a project template (see below).
2.  Deploy the required composite processes.
3.  Instantiate the resource types.
4.  Provide concrete instances of the atomic components.
5.  Execute.


## Project template {#project-template}

Although not strictly necessary, it is convenient to first set up a Scala project using [sbt](https://www.scala-sbt.org/) before deploying the code.

This is greatly facilitated by the provided [WorkflowFM Giter8 template for PEW](https://github.com/workflowfm/pew-deploy.g8). Assuming a working installation of sbt, you can set a project up using the following command:

```sh
sbt new workflowfm/pew-deploy.g8
```

This will prompt you for a _project name_ (among other options) and will build a directory with that name.

You can then deploy the generated code in the `*projectname*/src/main/scala` directory.

Use `sbt` to compile and run your new project.


## Configuration {#configuration}

The deployment dialog requires the following configuration options:

-   **Project name**: A general name for the project. This is used to name some of the higher level srructures in the code, such as the object containing the types.
-   **Process**: The stored composite process you want to deploy.
-   **Target directory**: The directory where the code will be placed.
-   **Package**: The name of the top level Scala package (namespace) in which the code should belong.
-   **Use Stateful library**: This should always be ticked in order to use the PEW library. Otherwise a deprecated/legacy library will be used.
-   **Create Main class**: Choose whether a template of a class containing a `main` method, such that instantiates and executes one instance of the deployed workflow, should be generated.

Once all the desired options are in, click on the `Done` button at the bottom of the window to start the deployment.

{{< tip >}}
Deployment of multiple compositions is not explicitly supported. However, it is safe to deploy more than one composition with the same option. Care must taken to ensure all resource types are instantiated. Uninstantiated types will be detected by the Scala compiler. We have plans to support larger and more complex deployments in future versions.
{{< /tip >}}


## Output {#output}

The output of the deployment is shown in the `Deployment Log` at the bottom half of the window. It can be split in 4 types:

1.  **Processes** (in the `processes` sub-package): Automatically generated code for each process. This is overwritten in every new deployment, so no user editing is expected here.
2.  **Instances** (in the `instances` sub-package): Templates for the atomic components. These are expected to be filled in  with code by the user. In case of a redeployment, these are **not** overwritten so as not to delete user code. However, extra care must be taken to ensure that a previously implemented process adheres to any changed specifications.
3.  **Types** (in the top level package): A package object including aliases of all required resource types as strings. The user can edit these to use their desired types. This file is also **not** overwritten.
4.  **Main** (in the top level package if selected): A class with a sample `main` method that instantiates and runs a single instance of the deployed workflow. This file is also **not** overwritten.