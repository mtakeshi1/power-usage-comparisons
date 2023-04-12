image: build/quarkus-app/quarkus-run.jar
	docker build -f src/main/docker/Dockerfile.jvm -t power/java .

build/quarkus-app/quarkus-run.jar:	$(shell find src -type f -name '*.java')
	./gradlew build
