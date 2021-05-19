Reddit-Wallpaper
======
##### Reddit-Wallpaper is tool to download and automatically set a random wallpaper from subreddits.
Bored by your old static wallpaper? Want to see something new? Well boyo this is the right place for you. 
This magnificent tool automatically downloads and sets up a wallpaper from the website reddit!
It allows you to decide the period of time after you get bored by a wallpaper and changes it, and you won't need to move a single muscle.
Best thing of all, it applies wallpapers using a non-repetitive pattern - meaning that it's unlikely that you will ever see the same wallpaper twice.
And it has so many more settings to customize!

Work in progress! But it does work and is stable!

**Main Features:**
- [Easy install](https://github.com/Mamiglia/Reddit-Wallpaper/blob/main/Installation.md)
- query reddit's API with the specified parameters
- select the wallpaper to download through a non-repetitive pattern
- Automatically apply such wallpaper 
- GUI to control settings
- System Tray
- Supports galleries!
- Dark Theme

#### Disclaimer
I need help! I'm kinda a beginner in Java development and in general as a software developer. For sure this project is full of hidden bugs, bad practices, unoptimal implementations and so on. Furthermore, the program has me as the only tester.
I need help, I need feedbacks, I need people to tell me that they like this/dislike that. Only with such support this tool will get better

**Known Bugs:**
- Doesn't support crossposts: they cause an error which blocks the setting of the wallpaper
- Clicking furiously while the program is elaborating something else may cause an error
- pinterest galleries aren't supported (yet)
- Sometimes it gives "Wallpaper not set issue" with no further explanation (fixed?)
- Sometimes wallpaper with no "preview" field are downloaded. At the moment the program skips them

**ToDo:**
- [ ] Add option to download only horizontal wallpapers
- [ ] Add option to download only wallpapers with a ratio similar to the screen user
- [x] Add a button to open wallpapers folder
- [ ] Add option to customize wallpaper folder during installation
- [ ] Improve logging
- [x] Add a button to erase wallpaper database 
- [ ] Add a minimum number of upvotes to consider a file
- [ ] Improve databasing with SQLite or smth
- [ ] Add a way to check for updates
- [ ] Improve memory usage (actuallly ca. 200mb)


