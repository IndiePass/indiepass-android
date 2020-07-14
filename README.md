# Indigenous for Android

An IndieWeb app with extensions for sharing information to micropub endpoints
and reading from microsub endpoints.

<a href='https://play.google.com/store/apps/details?id=com.indieweb.indigenous'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' height="100"/></a> <a href="https://f-droid.org/app/com.indieweb.indigenous"><img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"></a>

No builds will be uploaded anymore to GitHub, use F-Droid for that.

More information at https://indieweb.org/Indigenous and
https://indigenous.realize.be

Looking for the Desktop client? See https://github.com/swentel/indigenous-desktop

## Functionality

- Login with multiple domains, discover indieauth, micropub, microsub
  and media endpoints. The micropub and microsub endpoints are optional,
  but at least one should be available. Token revoke happens when deleting
  an account. PKCE support for additional security when authenticating.
- There's also a built-in anonymous user which allows reading channels and
  posts coming from https://indigenous.realize.be. Posts can be send to a
  custom endpoint and token.
- Micropub
  - post types: article, note, reply, repost, like, bookmark, event,
    issue, rsvp, geocache, read, checkin, trip and venue (h-card).
  - add multiple images, videos, audio, tags, toggle syndication targets
  - share location on note, article, event, checkin, geocache and venue
  - send image to media endpoint
  - apply filters, add text and emojis on images
  - Toggle post-status (published vs draft)
  - Save as local draft to finish later. This also includes coordinates,
    so you can finish later even when you are on a different location
    then. Place suggestions are also supported.
  - Allow autocomplete of usernames in body text
  - Manage contacts (add/delete/update)
  - Query, update and delete: get a list of posts and update basic
    properties (experimental)
- Microsub
  - read channels, with pull to refresh
  - read items per channel, with pull to refresh, response actions per item
  - reply, like, repost, rsvp, bookmark, upload or add feed directly
  - listen to audio or watch video if available 
  - view fullscreen images, zoom and pinch
  - Manage channels and feeds
  - Offline reading, optional via settings
- Share intents: receive text or images to directly share
- Push notifications: receive push notifications if you have an account
  on https://indigenous.realize.be

Checkin, Geocache and Venue are experimental and pass on all information
into a Geo URI in the geo property, e.g.

```
geo:51.5258325,-0.1359825,0.0;name=london;url=https://hwclondon.co.uk
```

## Screenshot

<img src="https://realize.be/sites/default/files/2019-02/1550590120900.jpg" width="400" />

More screenshots at https://indigenous.realize.be

## Known issues

#### Authentication loop

When authenticating for the first time, you might see a message
"Authentication successul" but then returning to the Sign in screen.
While the account has been created, the default was not set. In this
case, the sign in screen will allow you to select the account.

See https://github.com/swentel/indigenous-android/issues/84  
This happens sometimes, but not always.

#### Known syndication targets not parsed

See https://github.com/swentel/indigenous-android/issues/152

#### Authenticating

- Some browsers (e.g. the default browser on LineageOS), won't allow
  Indigenous to listen to the callback to verify the authorization code.
  Install a browser like Firefox (any flavor) so you can login.
- When your account requires 2FA, put the apps in split screen mode, see
https://github.com/swentel/indigenous-android/issues/210

#### Notifications don't arrive after the app is closed

This is due to new default settings in the latest Android versions. Try
turning of battery optimization for the Indigenous app and it should
work fine.

## Translations

Indigenous is currently only available in English. If you want to
translate to another language (or make the default English better), the
following file contains all strings used in the app. Either send a pull
request or open an issue with the file attached. Some strings contain '%s'.
These are placeholders and should be kept.

https://github.com/swentel/indigenous-android/blob/master/app/src/main/res/values/strings.xml

## Credits

This app uses following external libraries:

- https://github.com/burhanrashid52/PhotoEditor
- https://github.com/chrisbanes/PhotoView
- https://github.com/4eRTuk/audioview
- https://github.com/Karumi/Dexter
- https://github.com/bumptech/glide
- https://jsoup.org/download
- https://github.com/ticofab/android-gpx-parser

## Other Micropub and Microsub clients

There are ton of other (mobile) clients, see https://indieweb.org/Micropub/Clients and
https://indieweb.org/Microsub
