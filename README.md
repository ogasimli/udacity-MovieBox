# Movie Box
A highly functional and interactive android app developed by me for first and second stages of Android Nanodegree Program.

Movie Box was evaluated and graded as "Exceeds Specifications" by certified Udacity code reviewer.

## Features

The main features of the app:
* Discover the most popular, the highest rated movies or movies with the highest revenue
* Save favorite movies locally to view them even when offline
* Watch trailers
* Read user reviews

## The Movie Database API

Movie Box uses [The Movie Database](https://www.themoviedb.org/documentation/api) API to retrieve movies.
In order to be able to launch the app you have to get a valid API key from The Movie Database and add the following line to your gradle.properties file:

TheMovieDBAPIKey=YOUR_API_KEY

After that just replace YOUR_API_KEY with valid API key, obtained from The Movie Database.

## Screenshots

![screen](screenshots/main_phone.png)

![screen](screenshots/detail_phone.png)

![screen](screenshots/sw600dp_portrait.png)

![screen](screenshots/sw600dp_landscape.png)

![screen](screenshots/sw720dp_portrait.png)

![screen](screenshots/sw720dp_landscape.png)

## Libraries Used

* [ButterKnife](https://github.com/JakeWharton/butterknife)
* [Retrofit](https://github.com/square/retrofit)
* [ProviGen](https://github.com/TimotheeJeannin/ProviGen)
* [Glide](https://github.com/bumptech/glide)

## Android Developer Nanodegree
[![udacity][1]][2]

[1]: screenshots/nanodegree.png
[2]: https://www.udacity.com/course/android-developer-nanodegree--nd801

## License

    Copyright 2015 Orkhan Gasimli

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
