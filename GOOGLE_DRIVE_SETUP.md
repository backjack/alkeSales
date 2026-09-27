# Google Drive document storage

The Documents feature uses a Google service account to access one shared AlkeSales folder. AlkeSales users do not sign in to Google or grant Google consent.

## Google Cloud and Drive setup

1. Create or select a Google Cloud project and enable the Google Drive API.
2. Create a service account and generate a JSON key.
3. Create or select a Google Workspace **Shared Drive**, then create the `ALKE Documents` root folder inside it. Do not use a folder in an individual's My Drive: service accounts do not have My Drive storage quota.
4. Add the service account email address as a member of that Shared Drive with the **Content manager** role (or higher).
5. Copy the root folder ID from its Google Drive URL.

## Environment variables

Set these only on the Spring Boot server:

```text
GOOGLE_DRIVE_SERVICE_ACCOUNT_JSON_BASE64=base64-encoded-service-account-json
GOOGLE_DRIVE_ROOT_FOLDER_ID=shared-folder-id
```

`GOOGLE_DRIVE_ROOT_FOLDER_ID` must identify the folder created in the Shared Drive. The application creates its financial-year and month folders below this root.

To create the Base64 value in PowerShell without writing the key into the repository:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\secure\service-account.json'))
```

Store the resulting value as a secret in IntelliJ's Run Configuration for local development and in Render's environment settings for deployment. Never commit or log the service-account JSON.

Uploaded files are organized as:

```text
ALKE Documents/
  FY-2025-2026/
    Apr-25/
    May-25/
    ...
    Mar-26/
```

The selected document date determines both the financial year and month. April through December use the calendar year as the FY start; January through March use the previous year.
