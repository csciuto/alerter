=== INSTRUCTIONS ===

1. Copy alerter-template.properties and fill in the values there.
1a. This will require an email account to be processing from. Testing was done with an IMAP account.
1b. A Google Drive Application must be set up. See instructions at https://developers.google.com/identity/protocols/OAuth2 for how to obtain a client_secrets file for the OAuth2 flow.

2. Start the app. The only parameter is where to locate the properties file given in step 1. The first time it runs, Google will open a browser window to complete the OAuth2 flow.