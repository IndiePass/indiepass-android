# IndiePass for Android

An open social app with support for IndieWeb, Mastodon, Pleroma and Pixelfed.

<a href='https://play.google.com/store/apps/details?id=com.indieweb.indigenous'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="100"/></a> <a href="https://f-droid.org/app/com.indieweb.indigenous"><img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"></a>

More information at https://indieweb.org/IndiePass and
https://indiepass.app

## Features

- Login with multiple domains and account types:
  - IndieWeb: discover IndieAuth, micropub, microsub and media endpoints. The
    micropub and microsub endpoints are optional, but at least one should be
    available. Token revoke happens when deleting an account. PKCE support for
    additional security when authenticating.
  - Fediverse: Mastodon, Pleroma and Pixelfed.
- Comes with a built-in anonymous user which allows reading channels and
  posts coming from https://mastodon.social,
  https://pleroma.site or https://pixelfed.social. Posts can be sent to a custom
  endpoint and token.
- Posting
  - post types: article, note, reply, repost, like, bookmark, event,
    issue, rsvp, geocache, read, checkin, trip and venue (h-card).
  - add multiple images, videos, audio, tags, toggle syndication targets
  - share location on note, article, event, checkin, geocache and venue
  - send image to media endpoint
  - apply filters, add text and emojis on images
  - Toggle post-status (published vs draft)
  - Toggle sensitivity and visibility
  - Save as local draft to finish later. This also includes coordinates,
    so you can finish later even when you're on a different location
    then. Place suggestions are also supported.
  - Allow autocomplete of usernames in body text
  - Manage contacts (add/delete/update)
  - Query, update and delete: get a list of posts and update basic
    properties (experimental)
- Reader
  - read channels with pull to refresh
  - read items per channel, with pull to refresh, response actions per item
  - reply, like, repost, rsvp, bookmark, upload or add feed directly
  - listen to audio or watch video if available 
  - view fullscreen images, zoom and pinch
  - Manage channels and feeds
  - Offline reading, optional via settings
  - Main content becomes selectable after a long click
  - Browse by tag for Mastodon
- Share intents: receive text or images to directly share

Checkin, Geocache and Venue are experimental and pass on all information
into a Geo URI in the geo property, e.g.

```
geo:51.5258325,-0.1359825,0.0;name=london;url=https://hwclondon.co.uk
```

Some features are not available for Mastodon and Pixelfed, because they either
do not make sense or the API does not support it yet.
Pull requests welcome, of course!
To check authorizations of this app on your accounts, go to the following 
url on your instance:

Mastodon: oauth/authorized_applications
Pixelfed: settings/applications
Pleroma: not found yet (info welcome!)

## Screenshot

<img src="https://realize.be/sites/default/files/2019-02/1550590120900.jpg" width="400" />

## Known issues

#### Authentication loop

When authenticating for the first time, you might see a message
"Authentication successful" but then return to the Sign in screen.
While the account has been created, the default was not set. In this
case, the sign in screen will allow you to select the account.

See https://github.com/marksuth/indigenous-android/issues/84 
This happens sometimes, but not always.

#### Known syndication targets are not parsed

See https://github.com/marksuth/indigenous-android/issues/152

#### Authenticating

- Some browsers (e.g. the default browser on LineageOS), won't allow
  IndiePass to listen to the callback to verify the authorization code.
  Install a browser like Firefox (any flavor) so you can sign in.
- When your account requires 2FA, put the apps in split screen mode, see
https://github.com/marksuth/indigenous-android/issues/210

#### Notifications do not arrive after the app is closed

This is due to new default settings in the latest Android versions. Try
turning of battery optimization for the IndiePass app and it should
work fine.

## Translations

Thanks to everyone who has contributed to the translation project so far. The translation project will restart in 2022.

## API

If you'd like to add a new account type, checkout API.md for a quick guide.

## Credits

This app uses the following external libraries:

- https://github.com/burhanrashid52/PhotoEditor
- https://github.com/chrisbanes/PhotoView
- https://github.com/4eRTuk/audioview
- https://github.com/Karumi/Dexter
- https://github.com/bumptech/glide
- https://jsoup.org/download
- https://github.com/ticofab/android-gpx-parser
- https://github.com/hivemq/hivemq-mqtt-client

## Other IndieWeb and Fediverse clients

- Indieweb: https://indieweb.org/Micropub/Clients and https://indieweb.org/Microsub
- Mastodon: https://joinmastodon.org/apps
- Pleroma: https://docs.pleroma.social/clients.html

## Issues

If you find a bug, open a request.
Pull requests are welcome though!

Things I'd love to explore one day:

- allow posting to multiple accounts at once
- detail views for Fediverse
- cleaner account names (am.renameAccount())
- better UI/UX for managing feeds
- cleanup the various ways of requests

## Sponsors

I would like to extend many thanks to the following sponsors for funding development.

- [NLnet Foundation](https://nlnet.nl) and [NGI0
Discovery](https://nlnet.nl/discovery/), part of the [Next Generation
Internet](https://ngi.eu) initiative.
