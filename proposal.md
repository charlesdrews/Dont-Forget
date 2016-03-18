# Proposal: "Don't Forget" App

### App Objectives
* *Problems / Pain Points*
  * Forget to check weather before leaving for the day
  * Too much weather info can be overwhelming
  * Easy to forget other time-based events, like birthdays
* *User Goals*
  * Be reminded of key info (e.g. bring umbrella; wear boots) every morning
  * Get info automatically at consistent and customizable times

### Research Findings
* Most participants do base some decisions on weather forecast each day
* Users don't necessarily care about details or trends, just don't want to get caught in rain w/o umbrella
* Simple is better; comfortable w/ automatic notifications
* Other ideas suggested: birthdays, calendar (e.g. bring a lunch/don't bring a lunch), to-do list

### Target Audience
* Busy professionals w/ predictable daily schedule
* 20s-30s - comfortable w/ geolocation, notifications, etc.
* Interested in customization

### Features
* Pull weather data from one or more APIs based on device location, w/ option to use static zip code instead - run in background
* Gather info on what times a user is outside: morning commute, lunch, evening commute
* Use notifications to give timely reminders (don't forget an umbrella!) and curated weather details
* If user clicks thru from notifications to app, show full forecast detail
* Pull birthdays from Google contacts and/or Facebook - separate birthday reminders
* If time allows, incorporate lunch reminders based on Google calendar
* If time allows, add to-do list feature and/or tie to other to-do app (Keep, Asana, etc.)

### Differentiators
* Sweet spot of detail - more than just daily high/low temp, but not the full forecast detail
* Customizable automatic & predictable reminder times - not just storm notifications

### Constraints
* Time - MVP must be fully functional in two weeks
* Efficiency - if data syncs in background must be absolutely sure not to leak memory or tie up CPU
* Probably not able to incorporate user weather observations into forecast like some weather apps
* May not be able to tell if a lunchtime calendar event will provide food; unclear whether to remind user to bring or not bring lunch

### Extra Info