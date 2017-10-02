const electron = require('electron');
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
 
const path = require('path');
const url = require('url');

process.env.GOOGLE_API_KEY = 'AIzaSyDnjg3vk4EO8wd4ERE6LzzmUjdQ9FVKPvs';

let mainWindow;
 
function createWindow () {
    mainWindow = new BrowserWindow({width: 800, height: 600});
 
    mainWindow.loadURL(url.format({
        pathname: path.join(__dirname, 'www/index.html'),
        protocol: 'file:',
        slashes: true
    }));
 
    mainWindow.on('closed', function () {
        mainWindow = null
    })
}
 
app.on('ready', createWindow);
 
app.on('window-all-closed', function () {
    if (process.platform !== 'darwin') {
        app.quit()
    }
});
 
app.on('activate', function () {
    if (mainWindow === null) {
        createWindow()
    }
});

