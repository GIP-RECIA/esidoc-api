# Support documentation


## mvn commands

### To add NOTICE

`./mvnw notice:check` Checks that a NOTICE file exists and that its content match what would be generated.

`./mvnw notice:generate` Generates a new NOTICE file, replacing any existing NOTICE file.

### To add licence headers

`./mvnw license:check` verify if some files miss license header.

`./mvnw license:format` add the license header when missing. If a header is existing, it is updated to the new one.

`./mvnw license:remove` remove existing license header.

### To run server with properties outside of project scope

`./mvnw clean compile spring-boot:run`

## Setup git hooks

`git config core.hooksPath .githooks`