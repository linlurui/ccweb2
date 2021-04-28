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
import {GlobalVars} from "@/common/GlobalVars";
import DatasourceStore from '@/components/common/cctable/store/DatasourceStore'
const datasourceStore:DatasourceStore = getModule(DatasourceStore);

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "WorkspaceStore",
    store,
})
export default class WorkspaceStore extends VuexModule {
    public tabPages: Array<TabPageInfo>;
    public currentTab: string;
    public currentType: string;
    public dialogs: any = {
        showTestTable: false,
    }

    constructor(args: any) {
        super(args);
        this.tabPages = new Array<any>();
        this.currentTab = '';
        this.currentType = '';
    }

    @Mutation
    add(page: TabPageInfo) {
        this.tabPages.push(page)
    }

    @Mutation
    remove(id: string) {
        let page = (<Array<TabPageInfo>>this.tabPages).find(a=> a && a.id && a.id == id);
        if(!page) {
            return ;
        }
        let index = (<Array<TabPageInfo>>this.tabPages).indexOf(page);
        if(index < 0) {
            return;
        }

        let getEnableTab = (i:number) : any => {

            if(i<0) {
                return '';
            }

            if(!this.tabPages) {
              return '';
            }

            if(!this.tabPages[i]) {
                return getEnableTab(i-1);
            }

            return this.tabPages[i];
        }

        // TODO 关闭前保存
        if(id == this.currentTab) {
            let tab = getEnableTab(index-1);
            if(tab) {
                this.currentTab = tab.id;
                this.currentType = tab.type;
            }
        }
        else {
            //store里需要先把currentTab置为null再重新赋值TabPane才会被选中
            id = this.currentTab;
            this.currentTab = '';
            this.currentTab = id;
        }

        if(this.tabPages == undefined) {
          return '';
        }

        delete this.tabPages[index];
    }

    @Mutation
    select(id: string) {
        this.currentTab = id;
        let page = (<Array<TabPageInfo>>this.tabPages).find(a=> a && a.id == id);
        if(!page) {
            return ;
        }

        if(page.type == GlobalVars.TAB_TYPE_DATA) {
            datasourceStore.setCurrentTable(page.id);
        }
        this.currentType = page.type;
    }

    @Mutation
    clear() {
        this.currentTab = '';
        this.currentType = '';
        this.tabPages = [];
    }
}


export interface TabPageInfo {
    id: string;
    type: string;
    name: string;
    localhost: string;
    json?: any
}
