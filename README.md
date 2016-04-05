# <img src="images/icon.png" width="50"> Don't Forget

### Overview
Never get caught in the rain without an umbrella again! Don't Forget gives you timely notifications - customized to your daily routine - in three categories: weather, tasks/errands, and birthdays. These convenient reminders deliver essential data to make your life simpler.

##### Weather <img src="images/weather.png" width="175" align="right" />
Weather reminders utilize data from the [Weather Underground API] (www.wunderground.com/weather/api/) and provide the essential info you need before you leave the house - likelihood of rain or snow, current, and daily high temps. Clicking through from the notification to the app itself gives you a complete view of current conditions, hourly forecasts (36 hours), and daily forecasts (10 days). The user can also switch between Fahrenheit and Celsius at any time. The most recent weather data is saved locally so that it is available without waiting for a refresh, even when the device is offline.

Weather data is based on the device's current location, or if the user prefers, the app can use a manually-entered location instead. The app provides autocomplete location suggestions when a static location is being entered. If use of the phone's location has been disabled and a manually-entered location has not yet been provided, the user is notified and directed to the Settings screen.

##### Tasks <img scr="images/tasks.png" width="175" align="right" />
Task reminders are designed for time-sensitive errands. Need to mail a rent check on your way to work? No problem, add a "Before Work" reminder. Need to call your doctor during the day to schedule an appointment? Set a "Lunchtime" reminder. You can also choose "On the Way Home" for errands you need to run when you head out from work, and "Evening" for chores to do later. You can customize the time of day associated with each of those labels - set them once, then enjoy the simplicity of picking from four clear labels each time you create a task.

The times associated with each of the four time-of-day labels can be changed at any time via the Settings screen. On the tasks screen, when a task is checked off it automatically moves below the imcomplete tasks in the list. It can be unchecked, or removed by tapping the refresh icon in the toolbar. Task are saved to a local database and persist from session to session.

##### Birthdays <img src="birthdays" width="175" align="right" />
Birthday reminders connect seamlessly to your Google Contacts and make sure you never miss a chance to send a friend or loved one your best wishes. The notification arrive automatically and include each person's age so you can customize a birthday greeting. You can also easily add and update your contacts' birthdays right from the app - no need to open a separate contacts manager. Don't want to be reminded of some contacts' birthdays? No problem, easily turn birthday notifications on and off by person with a single tap.

The app provides autocomplete suggestions of your contacts' names when searching for a contact to update a birthday. Birthdays can be entered with or without a year, simply by checking or unchecking the "include year" box on the birthday input screen. If a year is not provided, the contact's age is gracefully omitted from notifications and the birthdays screen.

<img src="notification.png" width="175" align="right" /><img src="images/settings.png" width="175" align="right" />

### Technical Details
The three main screens - weather, tasks, and birthdays - are displayed via **Fragments** in a **ViewPager** and navigation is performed via swipe or by tapping the **TabLayout**. The floating action button belongs to the main activity, and fades in and out based on which fragment is active. Similarly, the on-click behavior of the floating action button and the refresh toolbar button vary automatically according to which fragment is active. There is also a settings screen which utilizes the **Preference widgets** provided by the Android SDK.

This app uses two different APIs from [Weather Underground] (www.wunderground.com/weather/api/), the **weather data API**, and the **location autocomplete API**. The device location is gathered from the **Google API Client** asynchronously, then is used as a paramater in calls to the weather data API. Those are made in a **Sync Adapter**; the results are parsed via **Retrofit**, and are then saved to a **Realm.io** database.

When this is complete, the sync adapter uses the **Content Resolver** to notify the **Content Observer** listening in the UI thread. The UI thread then queries the database and updates views, using **Picasso** to populate the weather icons via url. Hourly and daily weather forecast data are displayed in horizontally-scrolling **Recycler Views** within the vertically-scrolling weather fragment.

The manual location entry feature in settings uses an **Async Task** to repeatedly call the autocomplete API and suggest matches. All Settings changes are persisted to **Shared Preferences**. This was accomplished by extending the **AutoCompleteTextView** class and including a 

The task items are saved to the Realm database. New tasks are entered in an **Alert Dialog** which repurposes two **Number Picker** objects to select date and time of day. The four time of day options (before work, lunchtime, on the way home, and evening) are specified in a **Java Enum**. The preference widget to set the user preferences for these four times is an extension of the DialogPreference class using the TimePicker widget.

Birthdays are gathered from the **Contacts Provider** in an Async Task and persisted to the Realm database along with user notification preference for each contact. Birthdays can be added/edited right in the app, again via the Contacts Provider. Searching through contacts is done via another extension of AutoCompleteTextView, and a customized DatePicker.

Notifications are achieved via two **IntentService** classes. The first is a scheduler that uses the **Alarm Manager** to schedule executions of the second class - the notification service - according to the user's time of day preferences. The notification service gathers relevant data from Realm and launches Big Text Style notifications including only the items that are relevant at that date and time.

### Unsolved Problems & Major Hurdles
A short timeline was the biggest hurdle. I have a laundry list of additional features I play to implement for the next version of this app (they're in the "backlog" list in my Trello board). I also fell into a bit of "callback hell" in my weather fragment dealing with asynchronous connection to the Google API Client, asynchronous location requests (since the last known location was not nearly as readily available as the Google docs suggest), asynchronous API calls, and database queries. Some pen & paper flow-charting helped quite a bit, which is something I'll definitely remember for the future.

Regarding unsolved problems, I don't think there are any bugs, per se, but you may notice there is a preference in settings for which days of the week to receive notifications, but the app currently ignores this setting. There was no technical difficulty here; just ran out of time. Also, when the user opens the time of day preferences, they default to the current time rather than pre-populate with their saved values. Again, this was due to running out of time. 

### Links
* [Trello board](https://trello.com/b/6LysxRTr/project-4) - James & Drew were both added to the board
* [Play Store](https://play.google.com/store/apps) - approval pending

 
