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

import {ColumnInfo} from "@/components/common/cctable/store/CCTableStore";

class PostUtils {

    public getUrl(datasource: string, table: string, type?: string) {

        if(!datasource) {
            datasource = '{datasource}';
        }

        if(!table) {
            table = '{table}';
        }

        if(!type) {
            return `/api/${datasource}/${table}`;
        }

        switch (type.toLowerCase()) {
            case "login":
                return `/api/${datasource}/login`
            case "logout":
                return `/api/${datasource}/logout`
            case "insert":
                return `/api/${datasource}/${table}`
            case "update":
            case "delete":
            case "getById":
                return `/api/${datasource}/${table}/{id}`
            case "query":
                return `/api/${datasource}/${table}`
            case "count":
                return `/api/${datasource}/${table}/count`
            case "exist":
                return `/api/${datasource}/${table}/exist`
            case "join":
                return `/api/${datasource}/join`
            case "joinCount":
                return `/api/${datasource}/join/count`
            case "download":
                return `/api/${datasource}/download/${table}/{field}/{id}`
            case "preview":
                return `/api/${datasource}/preview/${table}/{field}/{id}/{page}`
            case "play":
                return `/api/${datasource}/play/${table}/{field}/{id}`
            case "upload":
                return `/api/${datasource}/${table}/{field}/upload`
            case "importExcel":
                return `/api/${datasource}/${table}/import`
            case "batchUpdate":
                return `/api/${datasource}/${table}/update`
            case "batchDelete":
                return `/api/${datasource}/${table}/delete`
            case "exportExcel":
                return `/api/${datasource}/${table}/export`
            case "exportJoin":
                return `/api/${datasource}/export/join`
            case "insertAndMax":
                return `/api/${datasource}/${table}/max/{field}`
            case "getLoginInfo":
                return `/api/${datasource}/session/user`
            case "sendMessage":
                return `/api/message/send`
            case "esSearch":
                return `/api/${datasource}/search/${table}`
            case "stream":
                return `/api/${datasource}/${table}/stream`
            case "saveAsPdf":
                return `/api/${datasource}/${table}/import/to/pdf`;
            case "sendMqttMessage":
                return `/api/mqtt/${datasource}/publish/${table}/{topic}/{qos}/{retained}`
        }
    }

    public getPostJson(type?: string, columns?: Array<ColumnInfo>) {
        if(!columns) {
            return {};
        }

        if(!type) {
            return { conditionList: this.getConditionList(columns) };
        }

        switch (type.toLowerCase()) {
            case "login":
                return {
                    "username": "用户名",
                    "password": "密码",
                }
            case "sendMessage":
                return {
                    "message": "消息内容", 
                    "receiver": {  
                        "groupId": "接收组",  
                        "roleId": "或 接收角色",   
                        "usernames": ["或 接收用户"] 
                    },
                    "sendMode": "发送方式: ALL(0, 'ALL'), USER(1, 'USER'), GROUP(2, 'GROUP'), ROLE(3, 'ROLE')"
                };
            case "logout":
            case "download":
            case "preview":
            case "getLoginInfo":
                return "";
            case "insert":
            case "insertAndMax":
                return [this.getData(columns)];
            case "sendMqttMessage":
            case "update":
                return this.getData(columns);
        }

        return {
            conditionList: this.getConditionList(columns),
        };
    }

    public getMethod(type: string | undefined) {
        if(!type) {
            return 'post';
        }
        switch (type.toLowerCase()) {
            case "logout":
            case "download":
            case "preview":
            case "getLoginInfo":
                return "get";
            case "insert":
            case "update":
            case "insertAndMax":
                return "put";
        }
        return 'post';
    }

    public getConditionList(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: Array<{name: string, value: any, algorithm: 'EQ'}> = new Array();
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result.push({
                name: column.key,
                value: column.value,
                algorithm: "EQ"
            });
        }

        return result;
    }

    public getSelectList(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: Array<{field: string, function: string, alias: string}> = new Array();
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result.push({
                field: column.key,
                function: "",
                alias: ""
            });
        }

        return result;
    }

    public getSortList(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: Array<{name: string, desc: boolean}> = new Array();
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result.push({
                name: column.key,
                desc: true
            });
        }

        return result;
    }

    public getKeywords(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: Array<{name: string, value: string}> = new Array();
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result.push({
                name: column.key,
                value: column.value
            });
        }

        return result;
    }

    public getPageInfo(pageIndex: number, pageSize: number) {
        return {
            "pageIndex": pageIndex, //页码
            "pageSize": pageSize  //每页条数
        }
    }

    public getGroupList(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: string[] = new Array();
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result.push(column.key);
        }

        return result;
    }

    public getData(columns: Array<ColumnInfo>) {
        if(!columns) {
            return [];
        }

        let result: any = {};
        for(let i=0; i<columns.length; i++) {
            let column = columns[i];
            result[column.key] = column.value;
        }

        return result;
    }

    public getHelpText(type?: string) {
        if(!type) {
            return "";
        }
        switch (type.toLowerCase()) {
            case "login":
                return `[Title]登录
                        [Item]URL：/api/{datasource}/login
                        [Item]请求方式：POST
                        [Item]POST参数：
                        [Code]{
                        [Code]  "username": "用户名",
                        [Code]  "password": "密码",
                        [Code]}`;
            case "logout":
                return `[Title]登出
                        [Item]URL：/api/{datasource}/logout
                        [Item]请求方式：GET`;
            case "insert":
                return `[Title]新增 (可批量)
                        [Item]URL：/api/{datasource}/{table}
                        [Item]请求方式：PUT
                        [Item]URL参数：{datasource},{table}为数据库表名称
                        [Item]POST参数：
                        [Code][
                        [Code]    {
                        [Code]      "字段名": "值",
                        [Code]      ...
                        [Code]    }
                        [Code]    ...
                        [Code]]`;
            case "update":
                return `[Title]修改
                        [Item]URL：/api/{datasource}/{table}/{id}
                        [Item]请求方式：PUT
                        [Item]URL参数：{table}为数据库表名称，{id}为主键
                        [Item]POST参数：
                        [Code]{
                        [Code]  "字段名": "值", 
                        [Code]  ...
                        [Code]}`;
            case "delete":
                return `[Title]删除
                        [Item]URL：/api/{datasource}/{table}/{id}
                        [Item]请求方式：DELETE
                        [Item]URL参数：{table}为数据库表名称，{id}为主键
                        [Item]POST参数：无`;
            case "getById":
                return `[Title]ID查询
                        查询与联合查询加密的字段不会解密显示，多用于列表，而ID查询的结果可以显示解密后内容，可用于保密详情。
                        [Item]URL：/api/{datasource}/{table}/{id}
                        [Item]请求方式：GET
                        [Item]URL参数：{table}为数据库表名称，{id}为主键
                        [Item]POST参数：无`
            case "query":
                return `[Title]查询
                        [Item]URL：/api/{datasource}/{table}
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]algorithm条件：EQ("="), GT(">"), LT("<"), GTEQ(">="), LTEQ("<="), NOT("<>"), NOTEQ("!="), LIKE, START, END, IN, NOTIN
                        [Item]POST参数：
                        [Code]{
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "id", //字段名 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "id",  //字段名 
                        [Code]        "function": "MAX", //数据库相关函数：MAX, MIN, UPPER, LOWER, LENGTH, AVG, COUNT, SUM等
                        [Code]    }, ... ]
                        [Code]}`;
            case "count":
                return `[Title]查询总数
                        [Item]URL：/api/{datasource}/{table}/count
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "id", //字段名 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "id",  //字段名 
                        [Code]        "function": "MAX", //数据库相关函数：MAX, MIN, UPPER, LOWER, LENGTH, AVG, COUNT, SUM等 
                        [Code]    }, ... ]
                        [Code]}`;
            case "exist":
                return `[Title]查询是否存在数据
                        [Item]URL：/api/{datasource}/{table}/exist
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "id", //字段名 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "id",  //字段名 
                        [Code]        "function": "MAX",  
                        [Code]    }, ... ]
                        [Code]}`;
            case "join":
                return `[Title]联表查询
                        [Item]URL：/api/{datasource}/join
                        [Item]请求方式：POST
                        [Item]URL参数：{datasource}为数据源ID
                        [Item]POST参数：
                        [Code]{
                        [Code]    "joinTables": [{
                        [Code]        "tablename": "salary",
                        [Code]        "alias": "a",
                        [Code]        "joinMode": "inner"
                        [Code]    }, {
                        [Code]        "tablename": "archives",
                        [Code]        "alias": "b",
                        [Code]        "joinMode": "Inner",
                        [Code]        "onList": [{ 
                        [Code]            "name": "b.id",   
                        [Code]            "value": "a.archives_id",   
                        [Code]            "algorithm": "EQ"
                        [Code]        }]
                        [Code]    }, ...],
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "id", //字段名 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "id",  //字段名 
                        [Code]        "function": "MAX",  
                        [Code]    }, ... ]
                        [Code]}`;
            case "joinCount":
                return `[Title]联表查询统计
                        [Item]URL：/api/{datasource}/join/count
                        [Item]请求方式：POST
                        [Item]URL参数：{datasource}为数据源ID
                        [Item]POST参数：
                        [Code]{
                        [Code]    "joinTables": [{
                        [Code]        "tablename": "salary",
                        [Code]        "alias": "a",
                        [Code]        "joinMode": "inner"
                        [Code]    }, {
                        [Code]        "tablename": "archives",
                        [Code]        "alias": "b",
                        [Code]        "joinMode": "Inner",
                        [Code]        "onList": [{ 
                        [Code]            "name": "b.id",   
                        [Code]            "value": "a.archives_id",   
                        [Code]            "algorithm": "EQ"
                        [Code]        }]
                        [Code]    }, ...],
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "id", //字段名 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...]
                        [Code]}`;
            case "download":
                return `[Title]下载文件
                        [Item]URL：/api/{datasource}/download/{table}/{field}/{id}
                        [Item]请求方式：GET
                        [Item]URL参数：{table}为数据库表名称，{field}为字段名，{id}为主键
                        [Item]POST参数：无`;
            case "preview":
                return `[Title]文件预览（支持预览图片、视频、PPT）
                        [Item]URL：/api/{datasource}/preview/{table}/{field}/{id}/{page}
                        [Item]请求方式：GET
                        [Item]URL参数：{table}为数据库表名称，{field}为字段名，{id}为主键，{page}为可选入参，可指定页码
                        [Item]POST参数：无`;
            case "play":
                return `[Title]视频播放
                        [Item]URL：/api/{datasource}/play/{table}/{field}/{id}
                        [Item]请求方式：GET
                        [Item]URL参数：{table}为数据库表名称，{field}为字段名，{id}为主键
                        [Item]POST参数：无`;
            case "upload":
                return `[Title]上传
                        [Item]URL：/api/{datasource}/{table}/{field}/upload
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称，{field}为字段名
                        [Item]POST参数：
                        [Code]表单：
                        [Code]    name1: 文件1
                        [Code]    name2: 文件2
                        [Code]    name3: 文件3
                        [Code]    ...
                        [Item]返回：
                        [Code]{
                        [Code]    name1: 相对路径1
                        [Code]    name2: 相对路径2
                        [Code]    name3: 相对路径3
                        [Code]}`;
            case "importExcel":
                return `[Title]导入excel
                        [Item]URL：/api/{datasource}/{table}/import
                        [Item]请求方式：POST
                        [Item]URL参数：{datasource}数据源,{table}为数据库表名称,{field}为要返回的字段名
                        [Item]POST参数：
                        [Code]表单：
                        [Code]    文件名1: 文件1
                        [Code]    文件名2: 文件2
                        [Code]    文件名3: 文件3
                        [Code]    ...
                        [Item]Excel文件格式：
                        需要导入的excel文件中新增一个名称为schema的sheet
                        schema的第一行为需要导入的表格表头
                        schema的第二行为对应数据库的字段名`;
            case "batchUpdate":
                return `[Title]批量查询更新
                        [Item]URL：/api/{datasource}/{table}/update
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "data": {
                        [Code]        "字段名": "值",
                        [Code]        ...
                        [Code]    },
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...]
                        [Code]}`;
            case "batchDelete":
                return `[Title]批量删除
                        [Item]URL：/api/{datasource}/{table}/delete
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code][id1, id2, ...]`;
            case "exportExcel":
                return `[Title]导出excel
                        [Item]URL：/api/{datasource}/{table}/export
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "name",    //字段名 
                        [Code]        "function": "MAX",   
                        [Code]        "alias": "姓名",    //别名，导出字段的表头名称，可以是中文
                        [Code]    }, ... ]
                        [Code]}`;
            case "exportJoin":
                return `[Title]联表查询导出excel
                        [Item]URL：/api/{datasource}/export/join
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "joinTables": [{
                        [Code]        "tablename": "salary",
                        [Code]        "alias": "a",
                        [Code]        "joinMode": "inner"
                        [Code]    }, {
                        [Code]        "tablename": "archives",
                        [Code]        "alias": "b",
                        [Code]        "joinMode": "Inner",
                        [Code]        "onList": [{ 
                        [Code]            "name": "b.id",   
                        [Code]            "value": "a.archives_id",   
                        [Code]            "algorithm": "EQ"
                        [Code]        }]
                        [Code]    }, ...],
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值
                        [Code]    }, ...],
                        [Code]
                        [Code]    "selectList": [{ //显示字段
                        [Code]        "field": "id",  //字段名 
                        [Code]        "function": "MAX",   
                        [Code]        "alias": "姓名",    //别名，导出字段的表头名称，可以是中文
                        [Code]    }, ... ]
                        [Code]}`;
            case "insertAndMax":
                return `[Title]新增(返回指定字段的最大值)
                        [Item]URL：/api/{datasource}/{table}/max/{field}
                        [Item]请求方式：PUT
                        [Item]URL参数：{datasource}数据源,{table}为数据库表名称,{field}为要返回的字段名,接口会返回该字段最后插入的值
                        [Item]POST参数：
                        [Code][
                        [Code]    {
                        [Code]      "字段名": "值",
                        [Code]      ...
                        [Code]    }
                        [Code]    ...
                        [Code]]`;
            case "getLoginInfo":
                return `[Title]获取当前登录用户
                        [Item]URL：/api/{datasource}/session/user
                        [Item]请求方式：GET`;
            case "sendMessage":
                return `[Title]Websocket消息推送
                        [Item]URL：/api/message/send
                        [Item]请求方式：POST
                        [Item]URL参数：无
                        [Item]POST参数：
                        [Item]表单：
                        {
                            "message": "my message", //消息内容
                            "receiver": {  //接收人
                                "groupId": "",  //组ID
                                "roleId": "",   //角色ID
                                "usernames": [] //用户名
                            },
                            "sendMode": "ALL"   //发送方式: ALL, USER, GROUP, ROLE
                        }`;
            case "esSearch":
                return `[Title]通过es搜索引擎查询数据
                        [Item]URL：/api/{datasource}/search/{table}
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        注意：使用该接口需要在application.yml配置中将elasticSearch.enable设为true，然后新增或修改数据时才会创建索引
                        [Code]{
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   //条件: EQ, GT, LT, GTEQ, LTEQ, NOT, LIKE, IN, NOTIN
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "max(id) as maxId", //格式类SQL的select子句写法，聚合函数参考Elasticsearch 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值(可写通配符*，中文通配符查询效果以分词准)
                        [Code]    }, ...]
                        [Code]}`;
            case "stream":
                return `[Title]数据滚动接口
                        [Item]URL：/api/{datasource}/{table}/stream
                        [Item]请求方式：POST
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Code]{
                        [Code]    "pageInfo" : {
                        [Code]        "pageIndex": 1, //页码
                        [Code]        "pageSize": 50  //每页条数
                        [Code]    },
                        [Code]
                        [Code]    "conditionList": [{ //查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1",   //值
                        [Code]        "algorithm": "EQ",   
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "sortList": [{ //排序条件
                        [Code]        "name": "id", //字段名 
                        [Code]        "desc": true  //true为降序，false为升序
                        [Code]    }, ... ],
                        [Code]
                        [Code]    "groupList" : [ //分组条件
                        [Code]        "max(id) as maxId", //格式类SQL的select子句写法，聚合函数参考Elasticsearch 
                        [Code]        ...
                        [Code]    ],
                        [Code]
                        [Code]    "keywords" : [{ //关键词模糊查询条件
                        [Code]        "name": "id",   //字段名
                        [Code]        "value": "1"   //值(可写通配符*，中文通配符查询效果以分词准)
                        [Code]    }, ...]
                        [Code]}`;
            case "saveAsPdf":
                return `[Title]导入文件并转为PDF接口
                        [Item]URL：/api/{datasource}/import/to/pdf
                        [Item]请求方式：POST|PUT
                        [Item]URL参数：{table}为数据库表名称
                        [Item]POST参数：
                        [Item]表单：
                        [Code]    save_full_text: true //是否全文索引，可选项
                        [Code]    字段: 文件
                        [Code]    ...`;
            case "sendMqttMessage":
                return `[Title]发布消息到MQTT服务器
                        [Item]URL：/api/mqtt/{datasource}/publish/{table}/{topic}/{qos}/{retained}
                        [Item]请求方式：POST
                        [Item]URL参数：{datasource}=数据源；{table}=数据库表名称；{topic}发布主题；{qos}=0：最多一次的传输，1：至少一次的传输，2：只有一次的传输；{retained}是否保留消息
                        [Item]POST参数（要发布的消息，JSON格式）：
                        [Code]{
                        [Code]    "字段名" : "数据",
                        [Code]    ...
                        [Code]}`;
        }

        return '';
    }
}

export default new PostUtils()
