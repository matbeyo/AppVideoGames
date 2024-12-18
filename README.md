# VideoGamesApp

VideoGamesApp is an Android application that helps users discover and track video games. The app integrates with the RAWG Video Games Database API to provide up-to-date information about games, including new releases, different genres, and detailed game information.

## Demo

Check out the app in action: [Demo Video](https://youtube.com/shorts/f3jpZPzEuNc?feature=share)

## Features

- **User Authentication**: Secure login and registration system using Firebase Authentication
- **Featured Games**: Showcase of notable video games
- **New Releases**: Latest game releases displayed in a scrollable list
- **Genre-based Browsing**: Games categorized by genres (Action, Adventure, RPG, etc.)
- **Search Functionality**: Search games with optional genre filtering
- **Favorites System**: Users can mark games as favorites and access them later
- **Game Details**: Detailed view for each game including images and descriptions
- **Responsive UI**: Clean and intuitive user interface with smooth navigation

## Technical Details

### Built With
- Android Studio
- Java
- Firebase Authentication
- Cloud Firestore
- RAWG Video Games Database API
- Picasso for image loading
- RecyclerView for efficient list displays
- Room Database for local storage

### Architecture
- Model-View-Controller (MVC) pattern
- Firebase for backend services
- REST API integration
- Local and remote data persistence

## Setup Instructions

1. Clone the repository
```bash
git clone https://github.com/matbeyo/VideoGames.git
```

2. Set up Firebase:
   - Create a new Firebase project
   - Add your Android app to Firebase project
   - Download `google-services.json` and place it in the app directory
   - Enable Email/Password authentication in Firebase Console

3. Get RAWG API Key:
   - Sign up at [RAWG](https://rawg.io/apidocs)
   - Get your API key
   - Create `apikey.properties` file in root directory
   - Add your API key: `RAWG_API_KEY=your_api_key_here`

4. Build and run the project in Android Studio



## Acknowledgments

- [RAWG Video Games Database](https://rawg.io/) for providing the game data API
- [Firebase](https://firebase.google.com/) for authentication and cloud services
- [Picasso](https://square.github.io/picasso/) for image loading and caching


Project Link: [https://github.com/matbeyo/VideoGames](https://github.com/matbeyo/VideoGames)
