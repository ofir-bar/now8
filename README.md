# Now8 - Native Android Client written in Java

## :exclamation:Note: This app is being rewritten in Kotlin from scratch, on a private repo. I keep the documentation for those interested to learn about this project. :exclamation:

## Now8 automates the process of notifying each passenger on their driver arrival
Use case: A group of 5 friends go out tonight. Bob is the driver; he can invite his friends to join the ride. Once Bob's friends join the ride, they will get notified a few minutes before Bob arrival to their location. Bob don't need to call each friend while driving and wait for him to get outside of his house.

## About this version
This version is the Android Mobile Client, there is also an iOS mobile client(to be released soon) and a backend side written in Python (Serverless)

<div align="center">
    <img src="https://i.imgur.com/3jkEzwG.png" width="250px"</img>
    <img src="https://i.imgur.com/SDnLcKj.jpg" width="250px"</img>
</div>

## Features, Tools and Design used
Frontend:
- This app follows the [Android MVP Architecture](https://antonioleiva.com/mvp-android/)
- [Retrofit library](https://github.com/square/retrofit) is used to make REST network requests to the backend
- [Scarlet library](https://github.com/Tinder/Scarlet) is used to create WebSocket connection to the database, this is to have backend-frontend communication in real time. The server needs to notify all the passengers on the driver location
- [RxJava](https://github.com/ReactiveX/RxJava) Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM
- [Auth0 library](https://github.com/auth0/Auth0.Android) is used to simplify the integration with [Auth0](https://auth0.com/) service, used for user login and authentication
- [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/intro) - to show the driver location on a map
- [Google Distance Matrix API](https://developers.google.com/maps/documentation/distance-matrix/start) - used to calculate ETA from the driver to a specific passenger.

Backend:
- All the backend infrastracture (Databases, API, etc) is written in AWS-specific code that translates to AWS infrastracture.
- This is possible using the [AWS Cloudformation](https://aws.amazon.com/cloudformation/).
- I use the [Serverless Framework](https://serverless.com/) to simplify the process and allow me to not being "locked down" to AWS.
Code-to-infrastracture can be easily duplicated to create the entire infrastracture from scratch in many areas on the world.

The server side uses AWS intensively and is written in Python(on a private repo):
- [AWS Lambda](https://aws.amazon.com/lambda/) to create all the required functions (create_ride, join_ride,etc)
- [AWS API Gateway](https://aws.amazon.com/api-gateway/) to trigger Lambda functions via the Mobile client, which must get Bearer token to run
- [AWS DynamoDB](https://aws.amazon.com/dynamodb/) stores the ride, and [DynamoDB Streams](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Streams.html) notifies all clients when a new passengers join the ride.
- [Auth0](https://auth0.com/) - This is the service that authenticates users.
- [Branch.io](https://branch.io/) - used to generate deep links to join a ride. Since it is not possible to know from what device the passengers will try to join a ride (iOS, Android) or even if they have the app installed, deep links are used.


## Now8 Workflow (only for creating & joining a ride)
<div align="center">
    <img src="https://i.imgur.com/h06AJXy.png"</img>
</div>

## License
```
Copyright 2018 Ofir Bar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
