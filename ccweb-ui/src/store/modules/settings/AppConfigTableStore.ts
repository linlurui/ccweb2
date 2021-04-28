/*
 *  CCWEB Copyright (C) 2020 linlurui <rockylin@qq.com>
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

import store from '../../index'
import {Module, VuexModule, Mutation, Action, getModule} from 'vuex-module-decorators'
import DatasourceStore from '@/components/common/cctable/store/DatasourceStore'
import HttpUtils from "@/common/utils/HttpUtils";
import {Button} from "iview";
const datasourceStore:DatasourceStore = getModule(DatasourceStore);
import {Message} from 'iview'

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "AppConfigTableStore",
    store,
})
export default class AppConfigTableStore extends VuexModule {
    public info: any = {};
    public status = {
        loading: false
    }

    @Mutation
    load(callback?: Function) {
        let table:string = datasourceStore.apiConfig.ccweb["app-config"].table;
        if(!table) {
            if(callback) {
                callback(this.info);
            }
            return;
        }
        let service: string = datasourceStore.apiConfig.spring.application.name;
        if(!service) {
            if(callback) {
                callback(this.info);
            }
            return;
        }

        this.status.loading = true;
        HttpUtils.post(table, {"conditionList": [{
            "name": "service",
            "value": service,
            "algorithm": "EQ"
        }]}).then((data: Array<any>)=>{
            this.info = {};
            for(let i=0; i<data.length; i++) {
                this.info[data[i].key] = data[i].value;
            }

            if(callback) {
                callback(this.info);
            }
        }).finally(()=>{
            this.status.loading = false;
        });
    }

    @Mutation
    set(data: {key:string, value:string}) {
        this.info[data.key] = data.value;
    }

    @Action
    public saveAll() {

    }

    @Action
    public save() {
        // 保存相关配置
        if(this.info['spring.application.name'] &&
            this.info['spring.application.name']!=datasourceStore.apiConfig.spring.application.name) {
            let destroy = (<any>Message).warning({
                duration: 0,
                closable: true,
                render: (h:Function) => {
                    return h('span', [
                        '服务名修改后所有设置将会被初始化，您确定要继续吗？',
                        h(Button, {
                            style: {
                                margin: '6px'
                            },
                            props: {
                                type: 'primary',
                                size: 'small'
                            },
                            on: {
                                click: ()=>{
                                    destroy();
                                    let oldServiceName = datasourceStore.apiConfig.spring.application.name;
                                    this.info['eureka.instance.instance-id'] = this.info['spring.application.name'];
                                    datasourceStore.apiConfig.eureka.instance['instance-id'] = this.info['spring.application.name'];
                                    datasourceStore.apiConfig.spring.application.name = this.info['spring.application.name'];
                                    datasourceStore.saveApiConfig(()=>{
                                        this.postServiceSetting().then(()=>{
                                            (<any>Message).success("设置成功");
                                        }).catch((e:any)=>{
                                            if(e && e.message && e.message.startsWith('ERR_ACTION_ACCESS_UNDEFINED:')) {
                                                return;
                                            }
                                            (<any>Message).error((e && e.message ? e.message : e));
                                            datasourceStore.apiConfig.spring.application.name = oldServiceName;
                                            datasourceStore.apiConfig.eureka.instance['instance-id'] = oldServiceName;
                                        });
                                    });
                                }
                            }
                        }, '确定')
                    ])
                }
            })
        }
        else {
            this.postServiceSetting().then(()=>{
                (<any>Message).success("设置成功");
            }).catch((e:any)=>{
                if(e && e.message && e.message.startsWith('ERR_ACTION_ACCESS_UNDEFINED:')) {
                    return;
                }
                (<any>Message).error((e && e.message ? e.message : e));
            });
        }
    }

    @Action
    async postData(data: any) {

        if(!data) {
            return Promise.resolve();
        }

        let keys: string[] = Object.keys(data);
        if(keys.length<1) {
            return Promise.resolve();
        }

        this.status.loading = true;
        let allPromise:Array<Promise<any>> = new Array();
        let allKeys: string[] = Object.keys(this.info);
        let putList: string[] = new Array();
        for(let i=0; i<keys.length; i++) {
            let key: string = keys[i];
            if(!key || !data[key]) {
                continue;
            }

            this.info[key] = data[key];
            if(allKeys.indexOf(key)>-1) {
                allPromise.push(this.postValue(key));
            }
            else {
                putList.push(key);
            }
        }

        if(putList.length>0) {
            allPromise.push(this.putValue(putList));
        }

        await Promise.all(allPromise).then(()=>{
            this.status.loading = false;
            let destroy = (<any>Message).success({
                duration: 0,
                closable: true,
                render: (h:Function) => {
                    return h('span', [
                        '变更设置需要重新启动程序才会生效',
                        h(Button, {
                            style: {
                                margin: '6px'
                            },
                            props: {
                                type: 'primary',
                                size: 'small'
                            },
                            on: {
                                click: ()=>{
                                    eval("require('electron').ipcRenderer.send('restart')");
                                }
                            }
                        }, '重启')
                    ])
                }
            }).catch((e:any)=>{
                this.status.loading = false;
                return Promise.reject(e);
            })
        })

        return Promise.resolve();
    }

    @Action
    async postServiceSetting() {
        try{
            let table:string = datasourceStore.apiConfig.ccweb["app-config"].table;
            if(!table) {
                (<any>Message).warning("找不到配置表名称！");
                return;
            }

            let service: string = this.info['spring.application.name'];
            if(!service) {
                (<any>Message).warning("请先设置服务名称！");
                return;
            }

            this.status.loading = true;
            let allPromise:Array<Promise<any>> = new Array();
            allPromise.push(this.postValue('spring.application.name'));
            allPromise.push(this.postValue('eureka.instance.instance-id'));
            allPromise.push(this.postValue('ccweb.security.admin.username'));
            allPromise.push(this.postValue('ccweb.security.admin.password'));
            allPromise.push(this.postValue('ccweb.security.encrypt.MD5.publicKey'));
            allPromise.push(this.postValue('ccweb.security.encrypt.AES.publicKey'));
            allPromise.push(this.postValue('ccweb.ip.whiteList'));
            allPromise.push(this.postValue('ccweb.ip.blackList'));
            allPromise.push(this.postValue('ccweb.limitTime.begin'));
            allPromise.push(this.postValue('ccweb.limitTime.end'));
            allPromise.push(this.postValue('ccweb.upload.basePath'));
            allPromise.push(this.postValue('ccweb.upload.mimeTypes'));
            allPromise.push(this.postValue('ccweb.upload.multiple'));
            allPromise.push(this.postValue('ccweb.upload.maxSize'));
            allPromise.push(this.postValue('ccweb.download.thumb.scalRatio'));
            allPromise.push(this.postValue('ccweb.download.thumb.fixedWidth'));
            allPromise.push(this.postValue('ccweb.download.thumb.watermark'));
            allPromise.push(this.postValue('eureka.client.serviceUrl.defaultZone'));
            await Promise.all(allPromise).then(()=>{
                this.status.loading = false;
                let destroy = (<any>Message).success({
                    duration: 0,
                    closable: true,
                    render: (h:Function) => {
                        return h('span', [
                            '变更设置需要重新启动程序才会生效',
                            h(Button, {
                                style: {
                                    margin: '6px'
                                },
                                props: {
                                    type: 'primary',
                                    size: 'small'
                                },
                                on: {
                                    click: ()=>{
                                        eval("require('electron').ipcRenderer.send('restart')");
                                    }
                                }
                            }, '重启')
                        ])
                    }
                }).catch((e:any)=>{
                    this.status.loading = false;
                    return Promise.reject(e);
                })
            })

            return Promise.resolve();
        }
        catch(e) {
            if(!e) {
                return Promise.reject('未知错误');
            }

            return Promise.reject(e);
        }
    }

    @Action
    private async postValue(key: string) : Promise<any> {
        if(!this.info[key]) {
            return Promise.resolve();
        }

        let table:string = datasourceStore.apiConfig.ccweb["app-config"].table;
        if(!table) {
            return Promise.reject(new Error("找不到配置表名称！"));
        }

        let service: string = this.info['spring.application.name'];
        if(!service) {
            return Promise.reject(new Error("请先设置服务名称！"));
        }

        await HttpUtils.post(table + "/update", { "data": { "value": this.info[key] },
            "conditionList": [{
                "name": "service",
                "value": service,
                "algorithm": "EQ"
            }, {
                "name": "key",
                "value": key,
                "algorithm": "EQ"
            }]
        }).then((reason: any)=> {
                Promise.resolve(reason);
        });
    }

    @Action
    private async putValue(keys: string[]) : Promise<any> {
        if(!keys || keys.length<1) {
            return Promise.resolve();
        }

        let table:string = datasourceStore.apiConfig.ccweb["app-config"].table;
        if(!table) {
            return Promise.reject(new Error("找不到配置表名称！"));
        }

        let service: string = this.info['spring.application.name'];
        if(!service) {
            return Promise.reject(new Error("请先设置服务名称！"));
        }

        let data: any[] = new Array();
        for(let i=0; i<keys.length; i++) {
            let key: string = keys[i];
            if(!this.info[key]) {
                continue;
            }

            data.push({service: service, key: key, value: this.info[key]});
        }

        await HttpUtils.put(table, data).then(()=> {});

        return Promise.resolve();
    }

    @Action
    public async removeKeys(keys: string[]) : Promise<any> {
        if(!keys || keys.length<1) {
            return Promise.resolve();
        }

        let table:string = datasourceStore.apiConfig.ccweb["app-config"].table;
        if(!table) {
            return Promise.reject(new Error("找不到配置表名称！"));
        }

        let service: string = this.info['spring.application.name'];
        if(!service) {
            return Promise.reject(new Error("请先设置服务名称！"));
        }


        let data: any[] = new Array();
        await HttpUtils.post(table, {"conditionList": [{
                "name": "service",
                "value": service,
                "algorithm": "EQ"
            }]}).then((config: Array<any>)=>{
            for(let i=0; i<keys.length; i++) {
                let key: string = keys[i];
                if(!key) {
                    continue;
                }
                let item: any = config.find((a:any)=>a.key == key);
                if(!item) {
                    continue;
                }

                data.push(item.id);
            }
        })

        if(Object.keys(data).length<1) {
            return Promise.resolve();
        }

        await HttpUtils.post(table + '/delete', data).then(()=> {
            for(let i=0; i<keys.length; i++) {
                delete this.info[keys[i]];
            }
        });

        return Promise.resolve();
    }
}
