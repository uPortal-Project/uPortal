# Docker and Kubernetes

uPortal has a vision of the future that includes pre-packaged ready-to-run
distributions that can be configured completely at runtime, as you would expect
any modern cloud-native application to be able to do. In support of this, there
have been discussions and work done on moving uPortal toward this goal,
including the building of two Dockerfiles: one container for the portal, and one
container for the database, whether used directly for non-production, or used to
initialize an external database using the dumped SQL files.

## Docker build

The docker build process, parameterized and managed primarily via Makefile, is
as follows:

1. Build the uPortal codebase, specifying a local directory as the build target.
1. Build an empty database docker image with a few needs for initializing an
   empty uPortal Postgres database.
1. Start the previously mentioned database in docker locally to be used for
   the uPortal init process
1. Query the docker daemon for the IP address of the locally-running database,
   and use this information to create a temporary
   filters/docker-build.properties file, inserting the IP address.
1. Use ant to initialize the database into the running docker postgres
   container.
1. Commit the database container, and run the Docker build for the uPortal
   container.
1. Push both containers to the named REGISTRY

## Kubernetes objects (in progress)

Requirements:
- Working kubernetes-compatible cluster
- Working kubelet binary and configuration
    - Must have permissions to create deployments, services, and ingresses in
        your chosen namespace.
    - It is assumed that the kubectl context has been pre-set to the desired
        target (`kubectl config use-context <your-context>`).

The kubernetes objects that are currently being created serve the purpose of
standing up a uPortal instance or cluster using the Open Source Kubernetes
project. Given that kubectl is configured to use your cluster, you can alter the
k8s/* files to your needs and `kubectl apply -f k8s` to deploy a uPortal
container into your cluster. If you have already created the kubernetes objects
in your cluster, you can use `make deploy` (and optionally `make deploy-db` or
`make deploy deploy-db`) to rebuild uPortal with any changes, and update the
images currently running in your cluster. If you are running multiple instances,
this should result in a rolling update which can be rolled back if it fails to
start properly.

## TODO list

- [ ] Fix certain runtime config issues such as local CAS server for login
    (currently not working)
- [ ] Better runtime configuration support
- [ ] Generic helm chart to factor out environment-specific values (e.g. DNS/https)
