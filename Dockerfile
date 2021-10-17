ARG PORT=7000

### Build Image ###
# Gradle image
FROM gradle:jdk10 as builder

# Build server
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :server:build --stacktrace

### Base Image ###
# Install OPAM and OCaml v4.07.
FROM ocaml/opam:debian-10-ocaml-4.07
ARG PORT
USER root

# Update registry and install common dependencies.
RUN apt-get update && \
    apt-get install -y \
        m4 \
        dos2unix \
        openjdk-11-jre-headless

USER opam
# Install library dependencies.
RUN opam install \
    num.1.3 camlp5.7.10 \
    json-wheel.1.0.6+safe-string

# Add & compile HOL-Light from source
ADD --chown=opam:opam server/hol-light /hol-light
WORKDIR /hol-light
RUN opam config exec -- make

# Copy the distribution from he builder and untar
COPY --from=builder --chown=opam:opam /home/gradle/src/server/build/distributions/WorkflowFM_Server-*.tar /server/server.tar
WORKDIR /server
RUN tar -xvf server.tar --strip-components 1

# Load necessary launch files.
ADD --chown=opam:opam server/scripts ./scripts
ADD --chown=opam:opam server/docker.properties .
RUN dos2unix scripts/*.sh scripts/*.ml docker.properties && \
    chmod -R +x scripts

# Run the Server and expose port.
EXPOSE ${PORT}
CMD bin/WorkflowFM_Server docker.properties

