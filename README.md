# Indigenous for Android

An app with extensions for sharing information to micropub endpoints and
reading from microsub endpoints.

Install from Google Play:
https://play.google.com/store/apps/details?id=com.indieweb.indigenous  
Install from F-Droid:
https://f-droid.org/en/packages/com.indieweb.indigenous/

No builds will be uploaded anymore to GitHub, use F-Droid for that.

More information at https://indieweb.org/Indigenous and
https://indigenous.realize.be

## Functionality

- Login with multiple domains, discover indieauth, micropub, microsub
  and media endpoints. The micropub and microsub endpoints are optional,
  but at least one should be available. Token revoke happens when deleting
  an account. PKCE support for additional security when authenticating.
- Micropub
  - post types: article, note, reply, repost, like, bookmark, event,
    issue, rsvp, geocache, checkin and venue (h-card).
  - add multiple images, videos, audio, tags, toggle syndication targets
  - share location on note, article, event, checkin, geocache and venue
  - send image to media endpoint
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
- Share intents: receive text or images to directly share
- GPS tracker: log your itineraries when walking, running, flying and so on.
  When posting to your site, the data will be send using the micropub
  endpoint as a trip.
- Push notifications: receive push notifications if you have an account
  on https://indigenous.realize.be

Checkin, Geocache and Venue are experimental and pass on all information
into a Geo URI in the geo property, e.g.

```
geo:51.5258325,-0.1359825,0.0;name=london;url=https://hwclondon.co.uk
```

## Screenshot

<img src="https://realize.be/sites/default/files/2019-02/1550590120900.jpg" width="400" />

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
following file contains all strings use in the app. Either send a pull
request or open an issue with the file attached. Some strings contain '%s'.
These are placeholders and should be kept.

https://github.com/swentel/indigenous-android/blob/master/app/src/main/res/values/strings.xml

## iOS

Deprecated - new apps are coming.

https://indigenous.abode.pub/ios/  
https://github.com/EdwardHinkle/indigenous-ios

## Other clients

https://github.com/pstuifzand/micropub-android
