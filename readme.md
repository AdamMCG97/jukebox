# Jukebox

### Description
This repo is the kotlin and spring boot backend for a simple web app integrating with the spotify api. Spotify users can start a jukebox session by authenticating with their spotify account. The user is then provided with a link to share with their friends for their session, allowing anyone with the link to search and add tracks to the hosts current spotify playback. 

One of the main use cases is for house parties or gatherings of larger groups which allows one person to give access to all attendees to queue music on a communal speaker system (playing from spotify) whilst limiting control of the playback and queue to the host on their device for skips and such. 

### ToDo
Further improvements to make:

* Add feature to export tracks queued in session as playlist for host or listener
* Output session link as QR code for ease of sharing
* Consider persisting jukebox sessions in datastore of some kind
* Add testing
* Build UI and integrate
* Consider concurrency as many elements are currently not threadsafe (prototype)
* Add feature to make private sessions that are password protected

