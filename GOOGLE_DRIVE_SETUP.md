# Google Drive document storage

The Documents feature uses a Google OAuth web application. OAuth is handled only by Spring Boot; Angular never receives the client secret, access token, or refresh token.

## Google Cloud setup

1. Create or select a Google Cloud project.
2. Enable the Google Drive API.
3. Configure the OAuth consent screen.
4. Create an OAuth 2.0 Client ID of type **Web application**.
5. Add an authorized redirect URI matching the deployed application, for example:
   `https://your-service.onrender.com/documents/google/callback`
6. Add the Google account that owns the document folder as a test user while the consent screen is in testing mode.

## Environment variables

Set these only on the Spring Boot server:

```text
GOOGLE_DRIVE_CLIENT_ID=your-client-id
GOOGLE_DRIVE_CLIENT_SECRET=your-client-secret
GOOGLE_DRIVE_REDIRECT_URI=https://your-service.onrender.com/documents/google/callback
```

Optionally set `GOOGLE_DRIVE_ROOT_FOLDER_ID` to store files below an existing folder or Shared Drive folder. Otherwise, the application creates an `ALKE Documents` root folder.

After deployment, sign in to ALKE, open **Documents**, and select **Connect Google Drive** once. The server stores the resulting OAuth refresh token in the application's MySQL database.

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
