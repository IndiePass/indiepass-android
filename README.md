# Indigenous for Android

An app with extensions for sharing information to micropub endpoints and reading from microsub endpoints.

BEWARE: This is still in extreme alpha, so things might break.
As soon as an alpha state is reached, we'll start deploying alpha releases.

## Current functionality

- Login with your domain, discover indieauth, micropub and microsub endpoints.
  The microsub endpoint is optional so you can simply use this as a micropub 
  client too.
- Micropub
  - post article
  - post note
  - post reply
  - post repost
  - post like
  - add image, tags
    (images are currently scaled to 1000px width/height)
- Microsub
  - read channels
  - read items per channel
  - reply, like, repost directly
  - go to external URL
  - listen to audio if available
  - view fullscreen images
- Share intents: receive text or images from other apps to directly share
- Syndication targets: reload, toggle favorite channels or reply/like/repost URL

## Sneak preview video

https://realize.be/notes/1333

## iOS

iOS version is available at https://github.com/EdwardHinkle/indigenous-ios

## Other clients

https://github.com/pstuifzand/micropub-android
