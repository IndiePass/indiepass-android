# Indigenous for Android

An app with extensions for sharing information to micropub endpoints and reading from microsub endpoints.

Install from Google Play: https://play.google.com/store/apps/details?id=com.indieweb.indigenous

More information is https://indieweb.org/Indigenous

## Functionality

- Login with multiple domains, discover indieauth, micropub and microsub endpoints.
  The microsub endpoint is optional so you can simply use this as a micropub 
  client too.
- Micropub
  - post types: article, note, reply, repost, like, bookmark, event and rsvp
  - add image, tags
  - share location on note, article and event
  - Toggle post-status (published vs draft)
- Microsub
  - read channels, with pull to refresh
  - read items per channel, with pull to refresh
  - reply, like, repost, rsvp or bookmark directly
  - go to external URL
  - listen to audio if available
  - view fullscreen images, zoom and pinch
- Share intents: receive text or images from other apps to directly share
- Syndication targets: reload, toggle favorite channels or reply/like/repost URL

## Known issues

#### Authentication loop

When authenticating for the first time, you might see a message "Authentication successul" but then returning to the Sign in screen. While the account has been created, the default was not set. In this case, the sign in screen will show a message:

*No default account was set, you can select one by pressing the "Set account" button.*

Underneath there will be a button which allows to set the default account. After that, you will go to the channel list (if a microsub endpoint is detected), or see the post types screen.

See https://github.com/swentel/indigenous-android/issues/84  
This happens sometimes, but not always.

## Sneak preview video

https://realize.be/notes/1333

## Screenshot

<img src="https://realize.be/sites/default/files/Screenshot_20180905-153456_Indigenous.jpg" width="400" />

## iOS

https://indigenous.abode.pub/ios/  
https://github.com/EdwardHinkle/indigenous-ios

## Other clients

https://github.com/pstuifzand/micropub-android
