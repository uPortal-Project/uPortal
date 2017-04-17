.PHONY: clean start-ctr db ear image deploy-db deploy

REGISTRY=andrewstuart
IMAGE=uportal
DB_IMAGE=updb
TAG=5

FQTN=$(REGISTRY)/$(IMAGE):$(TAG)
DB_FQTN=$(REGISTRY)/$(DB_IMAGE):$(TAG)

DB_TEMP_NAME=uportal-db-temp
DB_CTR_NAME=$(DB_TEMP_NAME)-ctr

BUILD_DIR:=build/docker
BUILD_ENV=docker-build

SHA=$(shell docker inspect --format "{{ index .RepoDigests 0 }}" $(1))
CTR_IP=$(shell docker inspect --format '{{ .NetworkSettings.IPAddress }}' $(DB_CTR_NAME))

build.properties:
	sed "s/@server.home@/$(shell sed 's/\//\\\//g' <<<$(BUILD_DIR))/g" build.properties.sample > build.properties

clean:
	-docker stop $(DB_CTR_NAME)
	-docker rm -v $(DB_CTR_NAME)
	-rm filters/$(BUILD_ENV).properties
	-mvn clean
	-./gradlew clean
	-mkdir -p $(BUILD_DIR)

# These must be run in a separate target, otherwise it seems `make` evaluates
# DB_IP before the container has been started.
start-ctr:
	docker build -t $(DB_TEMP_NAME) uportal-db
	docker run -e POSTGRES_USER=uportal -e POSTGRES_PASSWORD=password --name $(DB_CTR_NAME) -d $(DB_TEMP_NAME)

db: clean start-ctr
	sed s/updb/$(CTR_IP)/g < filters/docker.properties > filters/$(BUILD_ENV).properties
	ant -Dserver.base=$(BUILD_DIR) -Dmaven.test.skip=true -Denv=$(BUILD_ENV) initdb
	docker exec $(DB_CTR_NAME) /dumpdb.sh
	docker commit $(DB_CTR_NAME) $(DB_FQTN)
	docker push $(DB_FQTN)

ear: clean build.properties
	ant -Dserver.base=$(BUILD_DIR) -Denv=docker deploy-ear

image: ear
	docker build . -t $(FQTN)

push: image
	docker push $(FQTN)

deploy-db: db
	kubectl set image deployment/updb updb=$(call SHA,$(DB_FQTN))

deploy: image
	kubectl set image deployment/uportal uportal=$(call SHA,$(FQTN))
