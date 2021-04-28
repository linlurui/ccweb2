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

class SystemTableUtils {

    public getDefaultColumns(table: string | undefined) : Array<ColumnInfo> {
        if(!table) {
            return [];
        }

        let map:any = {
            "userGroupRole" : [
                { "key": "userGroupRoleId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "userId", "title":  "用户", "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "groupId", "title":  "分组", "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "group", key: "groupName", value: "groupId"
                    }},
                { "key": "roleId", "title":  "角色", "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "role", key: "roleName", value: "roleId"
                    }},
                { "key": "userPath", "title":  "路径", "type": "text", "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "modifyBy", "title":  "修改人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "modifyOn", "title":  "修改时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createOn", "title":  "创建时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createBy", "title":  "创建人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }}
            ],

            "user" : [
                { "key": "userId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "username", "title":  "用户名", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "key", "title":  "私钥", "type": "text", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "password", "title":  "密码", "type": "text", "display": false, "disabled": false, "canEdit": true, "value": "" },
                { "key": "type", "title":  "用户类型", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "status", "title":  "状态", "type": "select", "display": true, "disabled": false, "canEdit": true, "value": "",
                    "options": [{"text": "正常使用", "value": ""}, {"text": "账户冻结", "value": "1"}]
                },
                { "key": "modifyBy", "title":  "修改人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "modifyOn", "title":  "修改时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createOn", "title":  "创建时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createBy", "title":  "创建人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }}
            ],

            "role" : [
                { "key": "roleId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "roleName", "title":  "角色名", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "description", "title":  "描述", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "modifyBy", "title":  "修改人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "modifyOn", "title":  "修改时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createOn", "title":  "创建时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createBy", "title":  "创建人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }}
            ],

            "privilege": [
                { "key": "privilegeId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "groupId", "title":  "分组", "fixed": "left", "width":100, "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "group", key: "groupName", value: "groupId"
                    }},
                { "key": "roleId", "title":  "角色", "fixed": "left", "width":80, "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "role", key: "roleName", value: "roleId"
                    }},
                { "key": "aclId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "canAdd", "title":  "允许新增", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许新增", value: 1}] },
                { "key": "canDelete", "title":  "允许删除", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "" , "options": [{text: "允许删除", value: 1}]},
                { "key": "canUpdate", "title":  "允许更新", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许更新", value: 1}] },
                { "key": "canView", "title":  "允许查看详情", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许查看详情", value: 1}] },
                { "key": "canDownload", "title":  "允许下载", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许下载", value: 1}] },
                { "key": "canPreview", "title":  "允许预览文件", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许预览文件", value: 1}] },
                { "key": "canPlayVideo", "title":  "允许播放视频", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许播放视频", value: 1}] },
                { "key": "canUpload", "title":  "允许上传", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许上传", value: 1}] },
                { "key": "canExport", "title":  "允许导出", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许导出", value: 1}] },
                { "key": "canImport", "title":  "允许导入", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许导入", value: 1}] },
                { "key": "canDecrypt", "title":  "允许解密", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许解密", value: 1}] },
                { "key": "canList", "title":  "允许浏览", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许浏览", value: 1}] },
                { "key": "canQuery", "title":  "允许查询", "type": "checkbox", "dontSearch": true, "display": true, "disabled": false, "canEdit": true, "value": "", "options": [{text: "允许查询", value: 1}] },
                { "key": "scope", "title":  "允许范围", "fixed": "right", "width": 100, "ellipsis": true, "tooltip": true, "type": "select", "display": true, "disabled": false, "canEdit": true, "value": "",
                    "options": [{"text": "拒绝访问", "value": "-1"}, {"text": "允许访问", "value": "5"}, {"text": "允许访问自已的数据", "value": "0"},
                        {"text": "允许访问没有分组的数据", "value": "1"}, {"text": "允许访问本组的数据", "value": "2"},
                        {"text": "允许访问下级用户数据", "value": "3"}, {"text": "允许访问上级与下级数据", "value": "4"}]
                }
            ],

            "group" : [
                { "key": "groupId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "groupName", "title":  "分组名称", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "description", "title":  "描述", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "modifyBy", "title":  "修改人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "createBy", "title":  "创建人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "modifyOn", "title":  "修改时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createOn", "title":  "创建时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" }
            ],

            "appConfig" : [
                { "key": "service", "title":  "服务名", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "key", "title":  "键", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "value", "title":  "值", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" }
            ],

            "acl" : [
                { "key": "aclId", "isPrimaryKey": true, "title":  "主键", "type": "number", "dontSearch": true, "display": false, "disabled": false, "canEdit": false, "value": "" },
                { "key": "groupId", "title":  "分组", "type": "search", "display": true, "disabled": false, "canEdit": true, "value": "", "listTarget": {
                        table: "group", key: "groupName", value: "groupId"
                    }},
                { "key": "tableName", "title":  "表名", "type": "text", "display": true, "disabled": false, "canEdit": true, "value": "" },
                { "key": "modifyBy", "title":  "修改人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "createBy", "title":  "创建人", "type": "search", "display": true, "disabled": false, "canEdit": false, "value": "", "listTarget": {
                        table: "user", key: "username", value: "userId"
                    }},
                { "key": "modifyOn", "title":  "修改时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" },
                { "key": "createOn", "title":  "创建时间", "type": "date", "display": true, "disabled": false, "canEdit": false, "value": "" }
            ]
        }

        return map[table];
    }
}

export default new SystemTableUtils()
