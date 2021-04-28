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

import {Module, VuexModule, Mutation, Action, getModule} from 'vuex-module-decorators';
import store from "../..";
import {Message} from "iview";
import AppConfigTableStore from "@/store/modules/settings/AppConfigTableStore";
const appConfigTableStore = getModule(AppConfigTableStore)

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "ProfileSettingStore",
    store,
})
export default class ProfileSettingStore extends VuexModule {

    public form:any = {};

    public removedList: string[] = new Array();

    public status = {
        loading: false
    }

    constructor(args: any) {
        super(args)
    }

    @Action
    async save() {
        delete this.form[''];
        let keys: string[] = Object.keys(this.form);
        for(let i=0; i<keys.length; i++) {
            let key: string = keys[0].substring(keys[0].lastIndexOf('.')+1);
            if(!/^[a-zA-Z0-9]+$/.test(key)) {
                (<any>Message).warning('参数名只允许输入中英文、数字');
                return;
            }
        }

        if(this.removedList.length>0) {
            this.status.loading = true;
            await appConfigTableStore.removeKeys(this.removedList).then(()=>{
                this.removedList.splice(0, this.removedList.length);
            }).catch((reason: any)=>{
                if(reason.data && reason.data.message) {
                    (<any>Message).error(reason.data.message);
                    Promise.reject(reason);
                }
            }).finally(()=>{
                this.status.loading = false;
            })
        }

        if(keys.length > 0) {
            this.status.loading = true;
            await appConfigTableStore.postData(this.form).finally(()=>{
                appConfigTableStore.load();
            }).finally(()=>{
                this.status.loading = false;
            })
        }
    }

    @Action
    markRemoved(appendKey: string) {
        let keys: string[] = Object.keys(appConfigTableStore.info);
        if(keys && keys.indexOf(appendKey)) {
            if(this.removedList.indexOf(appendKey) == -1) {
                this.removedList.push(appendKey);
            }
        }

        return Promise.resolve({ removed: this.removedList, appConfig: appConfigTableStore.info });
    }
}
