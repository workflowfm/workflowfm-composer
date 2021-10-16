### Build Image ###
# Gradle image
FROM gradle:jdk10 as builder

# Build server
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ls
RUN gradle :server:build --stacktrace

### Base Image ###
# Install OPAM and OCaml v4.07.
# NOTE: Use OCaml image as Debian Opam installation is fragile.
FROM ocaml/opam2:4.07
USER root

ENV PORT=7000

# Update registry and install common dependencies.
RUN apt-get --allow-releaseinfo-change update && \
    apt-get install -y \
        m4=1.4.18-2 \ 
        build-essential=12.6 \
        dos2unix=7.4.0-1 \
        netcat \
        openjdk-11-jre-headless

# Install library dependencies.
RUN opam install \
    num.1.3 camlp5.7.10 \
    json-wheel.1.0.6+safe-string

# Add & compile HOL-Light from source
ADD server/hol-light /hol-light
WORKDIR /hol-light
RUN opam config exec -- make

# Copy the distribution from he builder and untar
COPY --from=builder /home/gradle/src/server/build/distributions/WorkflowFM_Server-*.tar /server/server.tar
WORKDIR /server
RUN tar -xvf server.tar --strip-components 1

# Load necessary launch files.
ADD server/scripts ./scripts
ADD server/docker.properties .
RUN dos2unix scripts/*.sh scripts/*.ml docker.properties && \
    chmod -R +x scripts

# Run the Server and expose port.
EXPOSE ${PORT}
CMD bin/WorkflowFM_Server docker.properties

