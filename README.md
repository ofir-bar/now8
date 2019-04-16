# Now8 - Native Android Client

## Now8 automates the process of notifying each passenger on their driver arrival
Use case: A group of 5 friends go out tonight. Bob is the driver; he can invite his friends to join the ride. Once Bob's friends join the ride, they will get notified a few minutes before Bob arrival to their location. Bob don't need to call each friend and wait for him to get outside of his house.

## About this version
This version is the Android Mobile Client, there is also an iOS mobile client(to be released soon) and a backend side.


## Features & Tools used
- All the backend code, including the backend infrastracture is written using the serverless framework
- AWS Lambda to create all the required functions (create_ride, join_ride,etc)
- AWS API Gateway to trigger Lambda functions via the Mobile clients, which must get Bearer token to run
- AWS DynamoDB (stores all the rides)
- AWS Cloudformation (used with the serverless framework. the backend infrastructure is written in code, which translates to AWS CloudFormation stacks)
- Auth0 - Used for signin/signup UI flow, and authorize API requests
- Branch.io - used to generate deep links to join a ride. Since it is not possible to know from what device the passengers will try to join a ride; deep links are required
- Google Maps SDK - to show the driver location on a map
- Google Matrix API - used to calculate ETA from the driver to a specific passenger.


## Now8 Architecture (only for creating & joining a ride)
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
