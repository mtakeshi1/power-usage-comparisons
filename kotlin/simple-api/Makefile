image: ./build/libs/mtakeshi1.githut.io.simple-api-all.jar
	docker build -t power/kotlin .

./build/libs/mtakeshi1.githut.io.simple-api-all.jar:	$(shell find src -type f -name '*.java')
	./gradlew build
