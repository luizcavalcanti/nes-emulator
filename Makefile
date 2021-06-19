clean:
	mvn clean

compile:
	mvn compile

package: compile
	mvn package

test: compile
	mvn test

ci-test: compile
	mvn --batch-mode --update-snapshots verify

inspect: compile
	mvn exec:java -Dexec.mainClass=nesemulator.ui.InspectionUI -Dexec.args=$(rom)

run: compile
	mvn exec:java -Dexec.mainClass=nesemulator.Main -Dexec.args=$(rom)