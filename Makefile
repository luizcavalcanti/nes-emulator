build:
	mvn package

test:
	mvn test

ci-test:
	mvn --batch-mode --update-snapshots verify

clean:
	mvn clean

inspect:
	mvn javafx:run

run: build
	java -jar target/nes-emulator-0.0.1-jar-with-dependencies.jar