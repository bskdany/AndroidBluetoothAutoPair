# AndroidBluetoothAutoPair

Failed project of an android bluetooth app that would auto pair and auto connect non protected speakers.

Project failed because it's not possible to click on the pairing confirmation popup unless the app has the BLUETOOTH_PRIVILEGED permission, to 
get that permission the app must be in the priv-app folder where some system apps are. Maybe if the phone is rooted it is possible to push it there with adb and maybe there is a way to sign the requested certificates to make everything work.

I also tought of an autoclicker but the overlay pauses when a system popup shows up. Until i get new ideas on how to make things work this project is closed.

Edit: it is possible to automate the click with Tasker and AutoInput
