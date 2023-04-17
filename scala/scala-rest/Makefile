image: app/build/libs/app-all.jar
	docker build -t power/scala .

app/build/libs/app-all.jar:	$(shell find app/src -type f -name '*.scala')
	./gradlew shadowJar
