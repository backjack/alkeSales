# Running ALKE Sales

ALKE Sales runs as one Spring Boot application. Spring Boot serves the compiled Angular UI and the API from the same port.

## Local startup

From the repository root, build the application:

```powershell
mvn package
```

Then start the packaged application:

```powershell
java -jar target/alkeSales.jar
```

Open [http://localhost:8080](http://localhost:8080). The Documents page and its `/documents` API use that same server and port.

Maven runs the Angular build with its managed Node.js runtime, so a separately installed Node.js version and `ng serve` are not required for this workflow. Re-run `mvn package` after frontend changes before restarting the JAR.

## Google Drive

Set `GOOGLE_DRIVE_SERVICE_ACCOUNT_JSON_BASE64` and `GOOGLE_DRIVE_ROOT_FOLDER_ID` on the Spring Boot process before starting it. See [GOOGLE_DRIVE_SETUP.md](GOOGLE_DRIVE_SETUP.md) for service-account setup details.
