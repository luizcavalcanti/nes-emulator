build:
	mvn package

test:
	mvn test

clean:
	mvn clean

run: build
	java -jar target/nes-emulator-0.0.1.jar