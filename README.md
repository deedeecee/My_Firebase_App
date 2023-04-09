# Firebase Android Authentication
This Android project allows users to register with their email address and password, and then authenticate themselves for further use. Firebase Authentication is used as the authentication backend.

## Files
This project contains the following files:

## MainActivity.java
This file contains the code for the login screen of the app. When the app is opened, the user will be prompted to enter their email ID and password, and then press the "login" button. If the user has not registered, they can click on the "register" text to go to the registration screen.

## onLoginClicked.java
This file contains the code for the dashboard screen of the app. After logging in, the user will be directed to this screen which will display their name, department, enrollment ID, email ID, and a welcome message.

## onRegisterClicked.java
The class creates an activity for user registration and sets the layout for it. It retrieves user input for registration details and performs validation on them. If the input is valid, it registers the user using the Firebase Authentication API and updates the user's display name. It also saves the user's data to the Firebase Realtime Database. Finally, it shows a success message to the user and redirects them to the login activity. If there is any error during the registration process, it shows an error message to the user.

## Requirements
Android Studio 4.1 or higher
Android SDK 29 or higher
Google Services SDK

## Installation
Clone the repository to your local machine.
Open Android Studio and select "Open an Existing Project".
Navigate to the project folder and select the project.
Build and run the project on an emulator or a physical device.
## Usage
After running the app, you will be directed to the login screen.
Enter your email ID and password.
Press the "login" button. If you have not registered, click on the "register" text.
If you are successfully authenticated, you will be directed to the dashboard screen where your details will be displayed.

## Credits
This project was created by Debankar Dutta Chowdhury.
