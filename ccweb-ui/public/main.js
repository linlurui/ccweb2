/*
 *  CCWEB Copyright (C) 2016 linlurui <rockylin@qq.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const electron = require('electron');
// Module to control application life.
const app = electron.app;

app.commandLine.appendSwitch('ignore-certificate-errors');

const globalShortcut = electron.globalShortcut;
// Module to create native browser window.
var BrowserWindow = electron.BrowserWindow;

const proxy = require('./proxy.js');

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;

let serviceRunning = false;
let isQuit = false;
let portsArr = [0,0];
let readyError = false;
let httpPort = 0;
let httpsPort=0;

electron.ipcMain.on('error', function(event, err) {
    if(err.code == "EADDRINUSE"){
        console.error('ERROR: listen port '+err.port+' has been used!');
        electron.dialog.showErrorBox('程序错误', '端口【'+err.port+'】已经被占用!');
    }else{
        console.error(err);
        electron.dialog.showErrorBox('程序错误', err);
    }
    app.quit();
});

electron.ipcMain.on('success', function(event, message) {
    console.error(message);
});

electron.ipcMain.on('restart', function() {
    app.relaunch();
    app.exit(0);
});

process.on('uncaughtException', function (err) {
    console.error(err);
    app.quit();
});

function onReady () {

    globalShortcut.register('CommandOrControl+Q', function () {
        app.quit();
    });

    //tray
    var appIcon = new electron.Tray(__dirname + '/logo3-20.png');
    const contextMenu = electron.Menu.buildFromTemplate([
        {label: '管理中心(Admin)', click:function(){
                createAdmin();
            }},
        {label:'-',type:'separator'},
        {label: '退出(Quit)',click:function(){
                app.quit();
            }}
    ]);
    appIcon.setToolTip('CCWEB Server Designer');
    appIcon.setContextMenu(contextMenu);
    appIcon.on('balloon-closed', function(){
        appIcon.displayBalloon();
    });
    appIcon.on('double-click', function(){
        console.log('double-click');
        createAdmin();
    });

    try
    {
        if(!serviceRunning) {

            serviceRunning = true;
            let allPromise = new Array();
            const packageConf = require('./package.json');
            let http = 8080;
            let https = 8088;
            if(packageConf && packageConf.server) {
                if(packageConf.server.httpPort) {
                    http = packageConf.server.httpPort;
                }
                if(packageConf.server.httpsPort) {
                    https = packageConf.server.httpsPort;
                }
            }
            allPromise[0] = getFreePort(http);
            allPromise[1] = getFreePort(https);

            console.log('before running service!!!');

            let watch = ()=>{
                if(readyError) {
                    return false;
                }

                let contains = function(arr, obj) {
                    var i = arr.length;
                    while (i--) {
                        if (arr[i] === obj) {
                            return true;
                        }
                    }
                    return false;
                }

                let tempArr = new Array();
                if(portsArr[0] && portsArr[1]) {
                    portsArr.sort(function (m, n) {
                        if (m < n) return -1
                        else if (m > n) return 1
                        else return 0
                    });

                    for(let i in portsArr) {
                        if(contains(tempArr, portsArr[i])) {
                            portsArr[i] = portsArr[i]+1;
                            continue;
                        }

                        tempArr.push(portsArr[i]);
                    }

                    var ports = {http: portsArr[0]  , https: portsArr[1]};

                    httpPort = ports.http;
                    httpsPort = ports.https;

                    proxy.start(ports);

                    createAdmin();
                }

                else {
                    setTimeout(watch, 500);
                }
            };

            setTimeout(watch, 500);
            Promise.all(allPromise).then().catch((e)=>{
                console.log(e);
                serviceRunning = false;
                readyError = true;
                reject();
            });
        }
    }
    catch(e)
    {
        console.log(e);
        return;
    }
}

function getFreePortByShell(port) {
    return new Promise((resolve, reject)=>{
        if(!port || port<0) {
            port = 8080;
        }

        let msg = '';
        var child = require('child_process');
        if(process.platform == 'darwin' || process.platform == 'linux') {
            var order='lsof -i :' + port;
            msg = child.execSync(order);
        }
        else {
            var order="netstat -aon | findstr '"+port+"'";
            msg = child.execSync(order);
        }

        console.log('msg: ' + msg);
        if(!msg || !msg.toString().trim()) {
            resolve(port);
        }
        else {
            port = port+1;
            getFreePortByShell(port);
        }
    });
}

function getFreePort(port){
    return new Promise((resolve, reject)=>{
        if(!port || port<0) {
            port = 8080;
        }

        const net = require('net');
        let server = net.createServer();
        server.on('listening',function(){
            for(let i=0; i<portsArr.length; i++) {
                if(!portsArr[i]) {
                    portsArr[i] = port;
                    break;
                }
            }
            server.close();
            resolve(port);
        });
        server.on('error',function(err){
            server.close();
            server = null;
            if(err.code == 'EADDRINUSE' || err.code == 'EACCES'){
                port = (port + 1);
                getFreePort(port);
            }
            else {
                console.log(err);
                app.quit();
            }
        });
        server.listen(port);
    });
}

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
function createAdmin(isHide){

    if(!isHide && mainWindow){
        mainWindow.show();
        return;
    }

    console.log('Create the browser window.');
    mainWindow = new BrowserWindow({width: 1024, height: 768, show:!isHide});

    const shouldQuit = app.makeSingleInstance((commandLine, workingDirectory) => {
        if (mainWindow) {
            if (mainWindow.isMinimized()) mainWindow.restore()
            mainWindow.focus()
        }
    })
    if (shouldQuit) {
        app.quit()
    }

    // and load the index.html of the app.
    mainWindow.loadURL('file://' + __dirname + '/index.html?httpPort=' + httpPort + '&httpsPort=' + httpsPort);

    // Open the DevTools.
    //mainWindow.webContents.openDevTools()

    mainWindow.on('close', function(event){
        if(isQuit || mainWindow==null) {
            return;
        }

        console.log('关闭窗口');
        event.preventDefault();
        mainWindow.hide();
    });

    // Emitted when the window is closed.
    mainWindow.on('closed', function (event) {
        mainWindow = null;
    });
}

app.on('before-quit', function () {
    // Stop server.
    proxy.stop(electron);
    isQuit = true;
    console.log('停止服务！');
});

app.on('will-quit', function () {
    // Unregister all shortcuts.
    globalShortcut.unregisterAll();
});

app.on('activate', function () {
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    createAdmin();
});

// Quit when all windows do not closed.
app.on('window-all-closed', function () {
    // On OS X it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        createAdmin(true);
    }
});

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', onReady);
