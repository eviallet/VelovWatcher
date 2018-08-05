# VelovWatcher
Anddroid homescreen widget to keep an eye on JCDecaux's Cyclocity stations. Using [osmdroid](https://github.com/osmdroid/osmdroid) awesome API as the main map, and the official [JCDecaux](https://developer.jcdecaux.com/#/opendata/vls?page=getstarted)'s API to parse real time infos.


<img src="homescreen.png" height="350" width="200"> <img src="city.png" height="350" width="200"> <img src="station.png" height="350" width="200"> <img src="contracts.png" height="350" width="200"> 

# Working
* Parsing real time informations of every pinned station about available bikes/bike stands
* Ability to choose from any affiliated city in the world
* Refresh data every 30mn, or when the update button is pressedx
* Configure pinned stations/change current city at any time

# TODO
* Sometimes, the widget won't refresh or show anything and randomly works again. Issue seems to appear mainly at reboot.
