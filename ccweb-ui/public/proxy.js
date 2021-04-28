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

// Module to create native browser window.

const electron = require('electron')
const BrowserWindow = electron.BrowserWindow;
let serverHost = new Array();
let hasError = false;

exports.start = function(ports){

    try{
        if(hasError) {
            return false;
        }

        console.log("proxy start...");

        var domain = require('domain');
        var EventEmitter = require('events').EventEmitter;

        var e = new EventEmitter();

        var timer = setTimeout(function () {
            e.emit('data');
        }, 10);

        var d = domain.create();
        d.on('error', function (err) {
            console.log(err.message + '\n');
            hasError = true;
        });

        var fn = function() {
            console.log('服务端口分配：' + JSON.stringify(ports));

            startService(ports);
        }

        d.add(e);
        d.add(timer);

        d.run(fn);
    }
    catch(e){
        console.log(e.stack);
    }
};

exports.stop = function(electron){

    if(!serverHost)
        return;

    for(var host in serverHost){
        if(!serverHost[host])
            continue;

        serverHost[host].close();
    }

    hasError = null;
    serverHost = new Array();
};

exports.runServer = function(){

    if(!serverHost)
        return;

    if(!serverHost['apiServer']) {
        openServiceWindow('apiServer');
    }
};

exports.closeServer = function(){

    if(!serverHost)
        return;

    if(serverHost['apiServer']) {
        serverHost['apiServer'].close();
    }
};

function startService(ports){
    openServiceWindow('apiServer', ports.http);
}

function openServiceWindow(domain, port){

    var type = 'api';
    var debug = false;
    serverHost[domain] = new BrowserWindow({width: 900, height: 700, modal: true, show: false, title: domain, webPreferences: {
        webviewTag:true,
        javascript: true,
    }})

    if(debug){
        console.log('Open the DevTools.')
        serverHost[domain].webContents.openDevTools();
    }

    var serverUrl = 'file://' + __dirname + '/server.html?type=' + type + '&port=' + port;
    // console.log('load url: ' + serverUrl)
    serverHost[domain].loadURL(serverUrl);
    serverHost[domain].on('close', function () {
        releasePort(port);
    });

    serverHost[domain].on('closed', function () {
        serverHost[domain] = null;
    });
}

function releasePort(port) {
    if(process.platform == 'darwin' || process.platform == 'linux'){
        var order='lsof -i :' + port;
        var child = require('child_process');
        child.exec(order, function(err, stdout, stderr) {
            if(err){ return console.log('端口'+port+'可能已经被释放：' + err.message); }
            stdout.split('\n').filter(function(line){
                var p=line.trim().split(/\s+/);
                var address=p[1];
                if(address!=undefined && address!="PID"){
                    child.exec('kill '+ address,function(err, stdout, stderr){
                        console.log('端口'+port+'释放成功！');
                    });
                }
            });
        });
    }
    else if(process.platform == 'win32'){
        var order="netstat -aon | findstr '"+port+"'";
        var child = require('child_process');
        child.exec(order, function(err, stdout, stderr) {
            if(err){ return console.log('端口'+port+'出错：' + err.message) }
            stdout.split('\n').filter(function(line){
                var p=line.trim().split(/\s+/);
                var address=p[0];
                if(address!=undefined && address!="PID"){
                    child.exec('taskkill /pid '+address+' -f',function(err, stdout, stderr){
                        console.log('端口'+port+'释放成功！');
                    });
                }
            });
        });
    }
}

