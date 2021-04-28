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
import {Message} from 'iview'
import HttpUtils from "@/common/utils/HttpUtils";

@Module({
    namespaced: true,
    stateFactory: true,
    dynamic: true,
    name: "CCTableStore",
    store,
})
export default class CCTableStore extends VuexModule {

    public optionsDist: any = {};

    @Action
    async filterOptions(target?: ListTargetInfo) {

        if(!target || !target.table ||
            !target.key || !target.value) {
            return Promise.resolve();
        }

        if(this.optionsDist[target.table]) {
            return Promise.resolve(this.optionsDist[target.table]);
        }


        let postData: any = {
            selectList: [{field: target.key}],
            groupList: [target.key]
        }
        if(target.key==target.value) {
            await HttpUtils.post(target.table, postData).then((data: any)=> {
                if(target && target.table) {
                    this.optionsDist[target.table] = data;
                }
            })
            return Promise.resolve(this.optionsDist[target.table]);
        }

        postData = {
            selectList: [{field: target.key}, {field: target.value}]
        }

        await HttpUtils.post(target.table, postData).then((data: any)=> {
            if(target && target.table) {
                this.optionsDist[target.table] = data;
            }
        })

        return Promise.resolve(this.optionsDist[target.table]);
    }

    @Action
    async search(param: {table?: string, pageIndex?:number, pageSize?:number, conditions?: Array<ConditionInfo>, sorts?: Array<SortInfo>}) {

        if(!param.table) {
            return Promise.reject(new Error('table name can not be empty!'));
        }

        let result: any = null;
        await HttpUtils.post(param.table, {
            pageInfo: {
                pageIndex: param.pageIndex,
                pageSize: param.pageSize,
            },
            conditionList: param.conditions,
            sortList: param.sorts,
        }).then((data: any)=> {
            result = data;
        })

        return Promise.resolve(result);
    }

    @Action
    async count(param: {table?: string, conditions?: Array<ConditionInfo>}) {

        if(!param.table) {
            return Promise.reject(new Error('table name can not be empty!'));
        }

        let result: any = null;
        await HttpUtils.post(param.table + '/count', {
            conditionList: param.conditions
        }).then((data: any)=> {
            result = data;
        })

        return Promise.resolve(result);
    }

    @Action
    async add(param: {table?: string, data?: Array<any>}) {
        if (!param.table) {
            return Promise.reject(new Error('table name can not be empty!'));
        }

        if (!param.data) {
            return Promise.reject(new Error('data can not be empty!'));
        }

        let result: any = null;
        await HttpUtils.put(param.table, param.data).then((data: any) => {
            result = data;
            (<any>Message).success('新增成功');
        }).catch((reason: any) => {
            if(reason && reason.data && reason.data.message) {
                (<any>Message).error(reason.data.message);
            }
            (<any>Message).error('新增失败');
        })

        return Promise.resolve(result);
    }

    @Action
    async remove(param: {table?: string, idlist?: Array<number>}) {
        if (!param.table) {
            return Promise.reject(new Error('table name can not be empty!'));
        }

        if (!param.idlist) {
            return Promise.reject(new Error('id can not be empty!'));
        }

        let result: any = null;
        await HttpUtils.post(param.table + "/delete", param.idlist).then((data: any) => {
            result = data;
            (<any>Message).success('删除成功');
        }).catch((reason: any) => {
            if(reason && reason.data && reason.data.message) {
                (<any>Message).error(reason.data.message);
            }
            (<any>Message).error('删除失败');
        })

        return Promise.resolve(result);
    }

    @Action
    async update(param: {table?: string, id?: string, data?: any}) {
        if (!param.table) {
            return Promise.reject(new Error('table name can not be empty!'));
        }

        if (!param.id) {
            return Promise.reject(new Error('id can not be empty!'));
        }

        if (!param.data) {
            return Promise.reject(new Error('data can not be empty!'));
        }

        let result: any = null;
        await HttpUtils.put(param.table + "/" + param.id, param.data).then((data: any) => {
            result = data;
            (<any>Message).success('更新成功');
        }).catch((reason: any) => {
            if(reason && reason.data && reason.data.message) {
                (<any>Message).error(reason.data.message);
            }
            (<any>Message).error('更新失败');
        })

        return Promise.resolve(result);
    }
}

export interface KeyValuePart {
    key: string;
    value: string;
}

export interface OptionInfo {
    text?: string;
    value?: string;
}

export interface ColumnInfo {
    key: string;
    isPrimaryKey: boolean;
    title: string;
    display: boolean;
    disabled: boolean;
    canEdit: boolean;
    fixed: string;
    loading?: boolean;
    render?: Function;
    renderHeader?: Function;
    className?: string;
    align?: string;
    ellipsis: boolean;
    tooltip: boolean;
    sortable: boolean;
    minWidth: number;
    value: string;
    dontSearch: boolean;
    type: 'text' | 'select' | 'checkbox' | 'date' | 'number' | 'search' | 'file' | 'decimal';
    dataType?: string,
    listTarget?: ListTargetInfo;
    options?: Array<OptionInfo>;
    optionsFilter?: Function;
    defaultLabel?: string | string[];
}
export interface ListTargetInfo {
    table: string;
    key: string;
    value: string;
}

export interface TableInfo {
    name: string;
    displayName?: string;
    columns?: Array<ColumnInfo>
}

export interface ConditionInfo {
    name: string;
    value: string;
    algorithm: 'EQ' | 'GT' | 'LT' | 'GTEQ' | 'LTEQ' | 'NOT' | 'NOTEQ' | 'LIKE' | 'START' | 'END' | 'IN' | 'NOTIN' | undefined;
}

export interface SortInfo {
    name: string;
    desc: boolean;
}