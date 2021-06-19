package:
	mvn package

compile:
	mvn compile

test:
	mvn test

ci-test:
	mvn --batch-mode --update-snapshots verify

clean:
	mvn clean

inspect: compile
	mvn exec:java -Dexec.mainClass=nesemulator.ui.InspectionUI -Dexec.args=$(rom)

run: compile
	mvn exec:java -Dexec.mainClass=nesemulator.Main -Dexec.args=$(rom)