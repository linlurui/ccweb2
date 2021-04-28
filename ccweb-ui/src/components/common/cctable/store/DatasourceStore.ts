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

import store from '../../../../store'
import {Module, VuexModule, Mutation, Action, getModule} from 'vuex-module-decorators'
import FileUtils from "../../../../common/utils/FileUtils"
import {Button, Message} from "iview"
import HttpUtils from "../../../../common/utils/HttpUtils"
import LowDbUtils from "../../../../common/utils/LowDbUtils"
import EnumUtils from "@/common/utils/EnumUtils"
import {GlobalVars} from "@/common/GlobalVars"
import WebConfigUtils from "@/common/utils/WebConfigUtils";

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "DatasourceStore",
    store,
})
export default class DatasourceStore extends VuexModule {

    public currentTablename: string = "";
    public tables: Array<TableInfo> = new Array();
    public showDatasourceDialog: boolean = false;
    public showDeleteDialog: boolean = false;
    public showTableDialog: boolean = false;
    public apiConfig: any = null;
    private yamlPath = 'service/conf/application.yml';
    private message: any = Message;
    constructor(e: any) {
        super(e)
        if (process.platform != 'win32' && !WebConfigUtils.getProduction()) {
            this.yamlPath = 'dist/' + this.yamlPath;
        }

        this.apiConfig = FileUtils.readYaml('./' + this.yamlPath);
        if (!this.apiConfig) {
            this.message.error('Api配置文件不存在');
        }
    }

    @Mutation
    openDatasourceDialog(callback?: Function) {
        this.showDatasourceDialog = true;
        if(callback) {
            callback();
        }
    }

    @Mutation
    close() {
        this.showDatasourceDialog = false;
        this.showTableDialog = false;
    }

    @Action
    getApiConfigValue(key: string) {
        if(!key) {
            return Promise.resolve('');
        }

        let keys: Array<string> = key.split('.');
        if(!keys) {
            return Promise.resolve('');;
        }

        let source:any = this.apiConfig;
        let value: string = '';
        for(let i=0; i<keys.length; i++) {
            if(i==keys.length-1) {
                value = source[keys[i]];
            }
            else {
                if(!source[keys[i]]) {
                    return Promise.resolve('');;
                }

                source = source[keys[i]];
            }
        }

        return Promise.resolve(value);
    }

    @Mutation
    setApiConfigValue(key: string, value: string) {
        if(!key) {
            return;
        }

        let keys: Array<string> = key.split('.');
        if(!keys) {
            return;
        }

        let source:any = this.apiConfig;
        for(let i=0; i<keys.length; i++) {
            if(i==keys.length-1) {
                source[keys[i]] = value;
            }
            else {
                if(!source[keys[i]]) {
                    return;
                }

                source = source[keys[i]];
            }
        }
    }

    @Action
    async load() : Promise<any> {
        await HttpUtils.post('tables').then((data: any) => {
            if(!data || !Array.isArray(data)) {
                return Promise.resolve();
            }

            const localDb = LowDbUtils.use('TableMap');
            for(let i=0; i<data.length; i++) {
                let table = localDb.getData(data[i]);
                let tmp = this.tables.filter(a=> a.tableName==data[i]);
                if(tmp && Array.isArray(tmp) && tmp.length>0) {
                    continue;
                }

                if(EnumUtils.isSystemTable(data[i])) {
                    continue;
                }

                if(table) {
                    this.tables.push(table);
                }

                else {
                    this.tables.push({tableName: data[i], description: data[i], columnInfos: [], removedColumns: []});
                }
            }
        })

        return Promise.resolve(this.tables);
    }

    @Mutation
    saveApiConfig(callback?: Function) {
        try{
            FileUtils.writeYaml('./' + this.yamlPath, this.apiConfig);
            if(callback) {
                callback();
                return;
            }
            this.message.success("保存成功");
        }
        catch (e) {
            this.message.error((e && e.message ? e.message : '未知错误'));
            return;
        }

        let destroy = this.message.info({
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
        })
    }

    @Mutation
    saveDatasource(param: {dsName: string, datasource: any}) {
        try {
            this.apiConfig.entity.datasource.activated = param.dsName;
            this.apiConfig.entity.datasource.environments[param.dsName] = param.datasource;
            FileUtils.writeYaml('./' + this.yamlPath, this.apiConfig);
            this.message.success("保存成功");
        }catch (e) {
            this.message.error((e && e.message ? e.message : '未知错误'));
            return;
        }

        let destroy = this.message.warning({
            duration: 0,
            closable: true,
            render: (h:Function) => {
                return h('span', [
                    '变更数据源设置需要重新启动程序才会生效',
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
        })
    }

    @Action
    async buildTable(form: any) {
        if (!form.columnInfos || !Array.isArray(form.columnInfos)) {
            return;
        }

        if(this.currentTablename) {
            form.tableName = this.currentTablename;

            if(form.removedColumns && Array.isArray(form.removedColumns) && form.removedColumns.length>0) {
                let pkIndex:number = form.removedColumns.findIndex((a:any)=>a.isPrimaryKey);
                if(pkIndex>-1) { //主键不允许删除
                    form.removedColumns.splice(pkIndex, 1);
                }
                await HttpUtils.post(form.tableName + '/build/table', form.removedColumns).then((data: any) => {
                    console.log('先移列表')
                })
            }
        }
        else {
            if(form.columnInfos.findIndex((a:any)=>a.isPrimaryKey)==-1) {
                form.columnInfos.push({
                    columnName: "id",
                    isPrimaryKey: true,
                    dataType: "INT",
                    isAutoIncrement: true,
                });
            }
        }

        let changeColumns:Array<any> = new Array();
        for(let i=0; i<form.columnInfos.length; i++) {
            let column = JSON.parse(JSON.stringify(form.columnInfos[i]));

            if(this.currentTablename) {
                if(!column.alterMode || column.alterMode == GlobalVars.AlterMode.NONE) {
                    continue;
                }
            }

            let dataType = column.dataType.toLocaleUpperCase();
            switch (dataType) {
                case "IMAGE":
                case "VIDEO":
                case "AUDIO":
                case "DOCUMENT":
                    column.dataType = "VARCHAR";
                    column.maxLength = 1024;
                    break;
                case "TEXT":
                    column.maxLength = 0;
                    break;
                case "VARCHAR":
                    column.maxLength = 2048;
                    break;
                case "LABEL":
                    column.dataType = "VARCHAR";
                    column.maxLength = 32;
                    break;
            }

            delete column["description"];

            changeColumns.push(column);
        }

        await HttpUtils.post(form.tableName + '/build/table', changeColumns).then((data: any) => {
            let action = '设置';
            if(!this.currentTablename) {
                action = '创建';
            }

            for(let i=0; i<form.columnInfos.length; i++) {
                form.columnInfos[i].alterMode = GlobalVars.AlterMode.NONE;
            }

            const localDb = LowDbUtils.use('TableMap');
            const old: any = localDb.getData(form.tableName);
            if(old && old.columnInfos) {
                const feseredFields: Array<any> = old.columnInfos.filter((a: any) => EnumUtils.isFeservedField(a.columnName));
                if (feseredFields && feseredFields.length) {
                    for (let i = 0; i < feseredFields.length; i++) {
                        if (form.columnInfos.findIndex((a: any) => a.columnName == feseredFields[i].columnName) > -1) {
                            continue;
                        }

                        form.columnInfos.push(feseredFields[i]);
                    }
                }
            }
            localDb.setData(form.tableName, form);


            (<any>Message).success('表[' + form.tableName + ']' + action + '成功');
        })

        return Promise.resolve(form);
    }

    @Action
    async getServerColumns(table: string | undefined) {
        if(!table) {
            return Promise.resolve([]);
        }

        let result:TableInfo = {tableName: table, description: table, columnInfos: [], removedColumns: []};
        const localDb = LowDbUtils.use('TableMap');
        let data:any = localDb.getData(table);
        if(data && data.columnInfos && Array.isArray(data.columnInfos) && data.columnInfos.length>0) {
            result = data;
            return Promise.resolve(result);
        }

        await HttpUtils.post(table + "/columns").then((data: any) => {
            if(!data || !Array.isArray(data)) {
                return;
            }
            result.columnInfos = data;
        })

        return Promise.resolve(result);
    }

    @Mutation
    dropTable(param: {table: string, callback: Function}) {
        if(!param.table) {
            (<any>Message).warning('请选择需要移除的数据表');
        }
        HttpUtils.post(param.table + '/drop/table').then((data: any) => {
            (<any>Message).success('表[' + param.table + ']移除成功');
            const localDb = LowDbUtils.use('TableMap');
            localDb.unset(param.table);
            let tableList = this.tables;
            let info:any = tableList.find(a=> a && a.tableName && a.tableName==param.table);
            let index:number = this.tables.indexOf(info);
            this.tables.splice(index, 1);
            this.currentTablename = '';
            if(param.callback) {
                param.callback();
            }
        })
    }

    @Mutation
    editTable(table: string) {
        this.currentTablename = table;
        this.showTableDialog = true;
    }

    @Mutation
    newTable() {
        this.currentTablename = "";
        this.showTableDialog = true;
    }

    @Mutation
    setCurrentTable(table: string) {
        this.currentTablename = table;
    }

    @Mutation
    openDeleteConfirm() {
        this.showDeleteDialog = true;
    }
}

export interface TableInfo {
    tableName: string;
    description?: string;
    columnInfos: Array<any>;
    removedColumns: Array<any>;
}
