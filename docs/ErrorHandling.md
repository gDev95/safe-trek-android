# Error Handling
## Facebook Login
### Error
When an error occurs during the Login the callback evoked by the login manager should build an Alert Window informing
the user of a problem and ask him to open the app again. After the user clicks "OK" tohen will close.
### Cancelled
When the user cancels the Login he will be see an Alert window informing him that the login is
mandatory in order to use the app. The app closes as the user hits "OK".
## Safe Trek Authorization
For now, there is little error handling done for the authorization process. There might be scenarios in which the app 
might crash unexpectedly.
## Permissions
### Location
Another essential aspect of the app is the use of location services. We need to obtain user permission before we can use 
the geolocation of the device. If the permission is not granted the user should see an alert dialog informing him that the app 
requires location services. After the user clicked "OK" the App will close. NEXT TIME when the user logs in he will be prompted 
grant permission again
## Facebook Post
### Find the nearest place to current location
In order to utilize Facebook's Places API we first need to find the best match of places according
to the geolocation of the user. The app builds the request in the onCreate method and in the request manager we will pass
in additionally a callback. Two scenarios are being handled, **(1)** their is no internet connection. Based on manual testing 
I found that the onResponse() method is evoked. However the onResponse method is null. The App catches the Null Pointer exception.
**(2)** an error occured with the location, see **onLocationError()** in the Places API under Request. This method
is evoked if there was a problem with the SDK's API. No Alarm is created and a message is displayed informing the user of a problem.

