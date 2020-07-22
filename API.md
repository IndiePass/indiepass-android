# API documentation

## Adding a new account type

To add a new account type, you will have to create a few classes which
have a dedicated purpose in the application. For inspiration, take a
look at the mastodon directory for an example.

- General class: general functionality and features
- Reader class: parsing responses, endpoints etc
- Post class: creating and deleting posts etc
- Auth class: syncing accounts and revoking tokens

Base classes are available which handle some default cases.

The Factory classes will need to be updated so your new account type
can be detected:

- GeneralFactory
- ReaderFactory
- PostFactory
- AuthFactory

The authentication dance for a new account type is managed in AuthActivity.java
This code will be updated in the future to become more abstract so more
functions can be moved to the respective account type auth class.

An authenticator service needs to created and registered in the Manifest.
