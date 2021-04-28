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

import {GlobalVars} from "@/common/GlobalVars";

class EnumUtils {

    public getType(name: string, defaultType?: string) {

        if(!name) {
            if(defaultType) {
                return defaultType;
            }

            else {
                return '';
            }
        }

        switch (name) {
            case "group":
            case "role":
            case "user":
            case "privilege":
            case "userGroupRole":
            case "acl":
                return GlobalVars.TAB_TYPE_USER;
            case "profileSetting":
            case "serviceSetting":
                return GlobalVars.TAB_TYPE_SERVICE;
            case "dev-doc":
                return GlobalVars.TAB_TYPE_DOCUMENT;
            case "agentSetting":
            case "systemLog":
            case "commonSetting":
            case "dictSetting":
            case "cctest":
            case "login":
            case "logout":
            case "insert":
            case "update":
            case "delete":
            case "query":
            case "getById":
            case "count":
            case "exist":
            case "join":
            case "joinCount":
            case "download":
            case "preview":
            case "play":
            case "upload":
            case "importExcel":
            case "batchUpdate":
            case "batchDelete":
            case "exportExcel":
            case "exportJoin":
            case "insertAndMax":
            case "getLoginInfo":
            case "sendMessage":
            case "esSearch":
            case "stream":
            case "saveAsPdf":
            case "sendMqttMessage":
                return GlobalVars.TAB_TYPE_INTERFACE;
            case "agentTest":
                return GlobalVars.TAB_TYPE_AGENT;
            case "cch5":
            case "ccapi":
            case "ios":
            case "android":
            case "wx":
            case "ali":
            case "qq":
            case "baidu":
            case "tt":
                return GlobalVars.TAB_TYPE_PUBLISH;
            case "template":
            case "lib":
                return GlobalVars.TAB_TYPE_MARKET;
        }

        if(defaultType) {
            return defaultType;
        }

        return GlobalVars.TAB_TYPE_DATA;
    }

    public getDisplayName(name: string) {
        if(!name) {
            return '';
        }

        switch (name) {
            case "design":
                return '设计';
            case "group":
                return '分组';
            case "userGroupRole":
                return '授权';
            case "role":
                return '角色';
            case "user":
                return '账号';
            case "privilege":
                return '权限';
            case "acl":
                return '访问';
            case "agentSetting":
                return '代理';
            case "dev-doc":
                return '开发文档';
            case "serviceSetting":
                return '服务设置';
            case "profileSetting":
                return '服务参数'
            case "cctest":
            case "agentTest":
                return '测试';
            case "dictSetting":
                return '字典';
            case "systemLog":
                return '日志';
            case "commonSetting":
                return '设置';
            case "cch5":
                return '春蚕云(H5)';
            case "ccapi":
                return '春蚕云(Api)';
            case "ios":
                return 'IOS';
            case "android":
                return '安卓';
            case "wx":
                return '微信';
            case "ali":
                return '阿里';
            case "qq":
                return 'QQ';
            case "baidu":
                return '百度';
            case "tt":
                return '今日头条';
            case "template":
                return '模板';
            case "lib":
                return '组件库';
            case "login":
                return '登录';
            case "logout":
                return '登出';
            case "insert":
                return '新增';
            case "update":
                return '修改';
            case "delete":
                return '删除';
            case "query":
                return '查询';
            case "getById":
                return 'ID查询';
            case "count":
                return '查询总数';
            case "exist":
                return '是否存在';
            case "join":
                return '联表查询';
            case "joinCount":
                return '联表查询总数';
            case "download":
                return '下载文件';
            case "preview":
                return '文件预览';
            case "play":
                return '视频播放';
            case "upload":
                return '上传文件';
            case "importExcel":
                return '导入Excel';
            case "batchUpdate":
                return '批量更新';
            case "batchDelete":
                return '批量删除';
            case "exportExcel":
                return '导出Excel';
            case "exportJoin":
                return '联表导出Excel';
            case "insertAndMax":
                return '新增并返回ID';
            case "getLoginInfo":
                return '获取登录用户';
            case "sendMessage":
                return '消息推送';
            case "esSearch":
                return '搜索引擎查询';
            case "stream":
                return '获取数据流';
            case "saveAsPdf":
                return '导入为PDF';
            case "sendMqttMessage":
                return '发布MQTT消息';
        }

        return name;
    }

    public isSystemTable(table: string | undefined) {
        if(!table) {
            return false;
        }
        switch (table.toLowerCase()) {
            case "appconfig":
            case "group":
            case "role":
            case "user":
            case "privilege":
            case "usergrouprole":
            case "acl":
            case "agentsetting":
            case "servicesetting":
            case "profileSetting":
            case "dev-doc":
            case "cctest":
            case "agenttest":
            case "systemLog":
            case "commonsetting":
            case "cch5":
            case "ccapi":
            case "ios":
            case "android":
            case "wx":
            case "ali":
            case "qq":
            case "baidu":
            case "tt":
            case "template":
            case "lib":
            case "sqlite_sequence":
            case "login":
            case "logout":
            case "insert":
            case "update":
            case "delete":
            case "query":
            case "getById":
            case "count":
            case "exist":
            case "join":
            case "joinCount":
            case "download":
            case "preview":
            case "play":
            case "upload":
            case "importExcel":
            case "batchUpdate":
            case "batchDelete":
            case "exportExcel":
            case "exportJoin":
            case "insertAndMax":
            case "getLoginInfo":
            case "sendMessage":
            case "esSearch":
            case "stream":
            case "saveAsPdf":
            case "sendMqttMessage":
                return true;
        }
        return false;
    }

    isFeservedField(columnName: string) {
        let reservedFields:Array<string> = ["id", "createBy", "createOn", "modifyBy", "modifyOn", "userPath"];
        if(reservedFields.indexOf(columnName)>-1) {
            return true;
        }

        return false;
    }
}

export default new EnumUtils()
