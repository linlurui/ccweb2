/*
 *  ccpg Server Copyright (C) 2020 linlurui <rockylin@qq.com>
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


const fs = require('fs')
const path = require('path')
const electron = require('electron')
const child = require('child_process');

function start(){

	console.log('start()======>>>');
	var matchType = document.URL.match(/type=(\w+)/);
	var type = matchType && matchType[1] ? matchType[1] : 'api';

	var matchPort = document.URL.match(/port=(\d+)/);
	var port = matchPort && matchPort[1] ? matchPort[1] : '80';

	process.on('uncaughtException', function (err) {
		console.log('uncaughtException=================>')
		electron.ipcRenderer.send('error', err);
	});

	try {
		if(type=='api') {
			startApiServer(electron, port);
		}

		else {
			electron.ipcRenderer.send('error', '服务没有启动！');
		}

	}
	catch (e) {
		console.log(type + ' server error ==================>');
		electron.ipcRenderer.send('error', e.toString());
	}
}

/**
 * 启动api服务
 * @param electron
 */
function startApiServer(electron, port) {
	electron.ipcRenderer.send('success', '获取配置信息...');
	var servicePath = __dirname + '/service';
	if (!fs.existsSync(servicePath)) {
		electron.ipcRenderer.send('error', '找不到api路径！');
		return;
	}

	if (!fs.existsSync(servicePath + '/conf/application.yml')) {
		electron.ipcRenderer.send('error', '找不到api配置文件！');
		return;
	}

	if (!fs.existsSync(servicePath + '/libs/easyexcel-2.1.3.jar') ||
		!fs.existsSync(servicePath + '/libs/entity.queryable-1.0.0-SNAPSHOT.jar') ||
		!fs.existsSync(servicePath + '/libs/rxjava-2.1.10.jar') ||
		!fs.existsSync(servicePath + '/libs/spring-context-5.2.5.RELEASE.jar')||
		!fs.existsSync(servicePath + '/libs/spring-context-support-5.2.5.RELEASE.jar')) {
		electron.ipcRenderer.send('error', '找不到程序依赖包！');
		return;
	}

	if (!fs.existsSync(servicePath + '/ccweb-start-2.0.0-SNAPSHOT.jar')) {
		electron.ipcRenderer.send('error', '找不到api应用程序！');
		return;
	}

	electron.ipcRenderer.send('success', '监听api服务端口============>' + port);
	// let msg = child.execSync('cd ' + servicePath + ' && nohup java -jar -Dserver.port='+port+' ccweb-start-2.0.0-SNAPSHOT.jar '+
	// 	'-Xms1024m -Xmx1024m -Xss256k -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 '+
	// 	'-Duser.timezone=GMT+08 >ccweb.log 2>&1 &');
	let msg = child.execSync('cd ' + servicePath + ' && java -jar -Dserver.port='+port+' ccweb-start-2.0.0-SNAPSHOT.jar '+
		'-Xms1024m -Xmx1024m -Xss256k -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 '+
		'-Duser.timezone=GMT+08 >ccweb.log 2>&1');
	electron.ipcRenderer.send('success', msg.toString());
}

/**
 * 路径是否存在，不存在则创建
 * @param {string} dir 路径
 */
function ensureDir(dir) {
	var isExists;
	try {
		isExists = fs.statSync(dir);
	} catch(e) {}
	//如果该路径且不是文件，返回true
	if(isExists && isExists.isDirectory()){
		return true;
	}else if(isExists){     //如果该路径存在但是文件，返回false
		return false;
	}

	//如果该路径不存在
	var tempDir = path.parse(dir).dir;      //拿到上级路径
	if(!tempDir) { //没有上级路径创建并返回
		fs.mkdirSync(dir);
		return true;
	}
	//递归判断，如果上级目录也不存在，则会代码会在此处继续循环执行，直到目录存在
	var status = ensureDir(tempDir);

	if(status){
		fs.mkdirSync(dir);
		return true;
	}
	return false;
}

start();
