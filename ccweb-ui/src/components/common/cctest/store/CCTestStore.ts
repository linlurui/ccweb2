/*
 *  CCPage Copyright (C) 2020 linlurui <rockylin@qq.com>
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

import store from '@/store/index'
import { Module, VuexModule, Mutation, Action } from 'vuex-module-decorators'
import HttpUtils from "@/common/utils/HttpUtils";
import {Message} from "iview";

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "CCTestStore",
    store,
})
export default class CCTestStore extends VuexModule {

    public ids: string[] = new Array();

    public key: number = 0;

    public form: any = {
        default: {
            id: '',
            url: '',
            params: [{
                key: '',
                value: '',
            }],
            paramJson: '',
            headers: [{
                key: '',
                value: '',
            }],
            response: '',
            request: '',
            paramType: 'JSON',
            method: 'post',
            loading: false,
            mode: '',
        }
    }

    @Mutation
    refreshKey() {
        this.key = new Date().getTime();
    }

    @Mutation
    post(id?: string) {

        let key: string = 'default';
        if(id) {
            key = id;
        }

        let url = this.form[key].url;
        if(url.startsWith(this.form[key].method)) {
            url = url.substring(this.form[key].method.length);
        }

        let config: any = {
            url: url,
            headers: {},
            data: {},
            params: {},
            method: this.form[key].method,
        }

        for(let i=0; i<this.form[key].headers.length; i++) {
            let item: any = this.form[key].headers[i];
            if(!item.key) {
                continue;
            }

            if(!item.value == undefined) {
                item.value = '';
            }

            config.headers[item.key] = item.value;
        }

        if(this.form[key].paramType == "FORM") {
            config.headers['Content-Type'] = 'application/x-www-form-urlencoded';
            config.data = new FormData();
        }
        else {
            config.headers['Content-Type'] = 'application/json; charset=utf-8';
        }

        if(this.form[key].mode=='json') {
            if(this.form[key].paramJson) {
                config.data = JSON.parse(this.form[key].paramJson);
            }
        }
        else {
            for (let i = 0; i < this.form[key].params.length; i++) {
                let item: any = this.form[key].params[i];
                if (!item.key) {
                    continue;
                }

                if (!item.value == undefined) {
                    item.value = '';
                }

                switch (this.form[key].paramType) {
                    case "URL":
                        config.params[item.key] = item.value;
                        break;
                    case "JSON":
                        config.data[item.key] = item.value;
                        break;
                    case "FORM":
                        config.data.append(item.key, item.value);
                        break;
                }
            }
        }

        //output request
        this.form[key].request = 'Url: ' + this.form[key].url + '\r\n';
        let paramList:string[] = new Array();
        let keys: string[] = Object.keys(config.params);
        for(let i=0; i<keys.length; i++) {
            paramList.push(keys[i] + '=' + config.headers[keys[i]]);
        }

        if(paramList.length > 0) {
            if(this.form[key].url.indexOf('?')>-1) {
                this.form[key].request += paramList.join('&');
            }
            else {
                this.form[key].request += '?' + paramList.join('&');
            }
        }

        this.form[key].request += 'Method: ' + this.form[key].method + '\r\n';
        this.form[key].request += 'Headers: \r\n';
        keys = Object.keys(config.headers);
        for(let i=0; i<keys.length; i++) {
            this.form[key].request += '\t' + keys[i] + ': ' + config.headers[keys[i]];
        }
        this.form[key].request += '\r\nData: \r\n';
        keys = Object.keys(config.data);
        for(let i=0; i<keys.length; i++) {
            this.form[key].request += '\t' + keys[i] + ': ' + config.data[keys[i]];
        }

        //ready to request
        this.form[key].loading = true;
        HttpUtils.request(config).then((reason: any)=>{
            (<any>Message).success('请求成功');
            if(!reason) {
                return;
            }
            this.form[key].response = JSON.stringify(reason,null,"\t");

        }).catch((reason:any)=>{
            if(!reason) {
                return;
            }

            if(reason.status) {
                (<any>Message).error('错误码：' + reason.status);
            }

            this.form[key].response = JSON.stringify(reason,null,"\t");
        }).finally(()=> {
            this.form[key].loading = false;
            this.key = new Date().getTime();
        })
    }

    @Mutation
    setValuesToList(e: {platform: string, node: string, data: any}) {
        let keys: string[] = Object.keys(e.data);
        let list: any[] = new Array();
        for(let i=0; i<this.form[e.platform][e.node].length; i++) {
            if(keys.indexOf(this.form[e.platform][e.node].key)) {
                list.push({
                    key: this.form[e.platform][e.node][i].key,
                    value: e.data[this.form[e.platform][e.node][i].key],
                })
            }
            else {
                list.push({
                    key: this.form[e.platform][e.node][i].key,
                    value: this.form[e.platform][e.node][i].value,
                })
            }
        }

        if(list.length > 0) {
            (<any[]>this.form[e.platform][e.node]).splice(0, this.form[e.platform][e.node].length);
            for(let i=0; i<list.length; i++) {
                (<any[]>this.form[e.platform][e.node]).push(list[i]);
            }
        }

        this.key = new Date().getTime();
    }
}
