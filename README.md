## Repository Scanner

This is a Spring Boot application named repository-scanner using gradle wrapper.

## Prerequisites

* **Java:** Ensure you have Java 17 or later installed. Verify with `java -version`.
* **Google Drive API Credentials:**
    1. Follow the Google Drive API Quickstart guide for Java: [https://developers.google.com/drive/api/quickstart/java](https://developers.google.com/drive/api/quickstart/java)
    2. Create OAuth client ID credentials for a desktop application.
    3. Download the credentials file in JSON format (usually named `credentials.json`).
    4. Place the downloaded `credentials.json` file in the `src/main/resources` directory of your apiscanner project.

## Building the Application

1. Open your terminal and navigate to the root directory of your `apiscanner` project.

2. Execute the following command:

   ```bash
   ./gradlew build
   ```

   This will build the application and create a single executable JAR file in the `build/libs` directory, named `repository-scanner-<version>.jar`.

## Running the Application

1. Navigate to the `build/libs` directory.

2. Run the JAR file:

   ```bash
   java -jar repository-scanner-<version>.jar
   ```

   Replace `<version>` with the actual version of your project.

### Additional Information

* **Development Mode:** For hot reloading, use:

   ```bash
   ./gradlew bootRun
   ```
