<script lang="ts">
    import {Vue, Component, Watch} from 'vue-property-decorator';
    import {getModule} from "vuex-module-decorators"
    import DatasourceStore, {TableInfo} from '@/components/common/cctable/store/DatasourceStore'
    const datasourceStore = getModule(DatasourceStore);
    import CCInput from "@/components/common/ccinput/index.vue"
    import {Button, Message} from "iview";
    import EnumUtils from "@/common/utils/EnumUtils"
    import WorkspaceStore from '@/store/modules/workspace/WorkspaceStore'
    const workspaceStore = getModule(WorkspaceStore)
    import {GlobalVars} from "@/common/GlobalVars"
    import router from "@/router/.invoke/router";

    @Component
    export default class AddTablePopup extends Vue {

        public visible:boolean = false;
        public loading:boolean = false;

        public tableForm: TableInfo = {
            tableName: "",
            description: "",
            columnInfos: [],
            removedColumns: [],
        }

        public tableValidateRules:any = {
            tableName: [
                { required: true, message: '表名不能为空', trigger: 'blur' },
                { max: 10, message: '不能超过10个字符', trigger: 'blur' },
                { type: 'string', message: '只允许输入英文字母', pattern: /^[a-zA-Z][a-zA-Z\d]*$/, trigger: 'blur' }
            ],
            description: [
                { max: 10, message: '不能超过10个字符', trigger: 'blur' },
                { type: 'string', message: '不允许输入特殊字符', pattern: /^[a-zA-Z\u4e00-\u9fa5]+$/, trigger: 'blur' }
            ]
        }

        public datasourceForm: any = {
            dsName: "",
            dbName: "",
            type: "",
            domain: "",
            path:"",
            port: "3306",
            username: "",
            password: "",
        }

        public datasourceValidateRules:any = {
            dsName: [
                { required: true, message: '数据源名称不能为空', trigger: 'blur' },
                { max: 10, message: '不能超过10个字符', trigger: 'blur' },
                { type: 'string', message: '只允许输入英文或数字，并且不能以数字开始', pattern: /^[a-zA-Z][a-zA-Z0-9]*$/, trigger: 'blur' }
            ],
            dbName: [
                { required: true, message: '数据库名称不能为空', trigger: 'blur' },
                { max: 10, message: '不能超过10个字符', trigger: 'blur' },
                { type: 'string', message: '只允许输入英文或数字，并且不能以数字开始', pattern: /^[a-zA-Z][a-zA-Z0-9]+$/, trigger: 'blur' }
            ],
            type: [
                { required: true, message: '请选择数据库类型', trigger: 'blur' }
            ],
            domain: [
                { required: true, message: '数据库地址不能为空', trigger: 'blur' },
                { type: 'string', message: '非法数据库地址', pattern: /^([a-zA-Z][a-zA-Z0-9\.\-]+|\d+\.\d+\.\d+\.\d+)$/, trigger: 'blur' }
            ],
            port: [
                { required: true, message: '请输入端口', trigger: 'blur' },
                { type: 'number', message: '端口号必须输入大于零的数字', pattern: /^[1-9]\d*$/, trigger: 'blur' }
            ],
            username: [
                { required: true, message: '请输入用户名', trigger: 'blur' }
            ],
            password: [
                { required: true, message: '请输入数据库密码', trigger: 'blur' }
            ]
        }

        mounted(): void {
            let key: string = datasourceStore.apiConfig.entity.datasource.activated;
            let datasource: any = datasourceStore.apiConfig.entity.datasource.environments[key];
            this.datasourceForm.dsName = key;
            this.datasourceForm.type = datasource.driver;
            if(this.datasourceForm.type == 'org.sqlite.JDBC') {
                this.datasourceForm.path = datasource.url.replace(/jdbc:sqlite:(\w[\w\d\.\-\/\\]+)/, "$1");
            }
            else {
                let regex: RegExp = new RegExp('jdbc:(oracle:thin|\\w[\\w\\d\\-\\.]+)(://|:@)(\\d+\\.\\d+\\.\\d+\\.\\d+|\\w[\\w\\d\\-\\.]+):(\\d+)((/|;database=|:)(\\w[\\w\\d]+))?');
                let urls:Array<string> = datasource.url.split('?');
                this.datasourceForm.domain = urls[0].replace(regex, "$3");
                this.datasourceForm.port = urls[0].replace(regex, "$4");
                this.datasourceForm.dbName = urls[0].replace(regex, "$7");
            }
            this.datasourceForm.username = datasource.username;
            this.datasourceForm.password = datasource.password;
        }

        @Watch("$store.state.DatasourceStore.showTableDialog")
        onShowTableDialog(isShow: boolean) {
            if(!isShow) {
                return;
            }

            if(!datasourceStore.currentTablename) {
                this.tableForm.tableName = '';
                this.tableForm.columnInfos = [];
                this.tableForm.removedColumns = [];
                return;
            }

            this.tableForm.tableName = datasourceStore.currentTablename;
            datasourceStore.getServerColumns(this.tableForm.tableName).then((data: any) => {
                if(!data || !data.columnInfos || !Array.isArray(data.columnInfos)) {
                    return;
                }
                this.tableForm.columnInfos = [];
                this.tableForm.removedColumns = [];
                for(let i=0; i<data.columnInfos.length; i++) {
                    if(EnumUtils.isFeservedField(data.columnInfos[i].columnName)) {
                        continue;
                    }

                    data.columnInfos[i].alterMode = GlobalVars.AlterMode.NONE;

                    this.tableForm.columnInfos.push(data.columnInfos[i]);
                }
            })
        }

        onAdd(e: any) {
            this.tableForm.columnInfos.push({
                columnName: "",
                description: "",
                dataType: "",
                defaultValue: "",
                canNotNull: false,
                unique: false,
                isAutoIncrement: false,
                isPrimaryKey: false,
                alterMode: GlobalVars.AlterMode.ADD,
            });
        }

        openTableSettingDialog() {
            this.visible = false;
            datasourceStore.newTable();
        }

        buildTable() {
            (<any>this.$refs.tableForm).validate((valid: any)=>{
                if(!this.tableForm.columnInfos || this.tableForm.columnInfos.length<1) {
                    (<any>Message).warning('请设定义数据表的列属性');
                    datasourceStore.showTableDialog = true;
                    return false;
                }

                if(EnumUtils.isSystemTable(this.tableForm.tableName)) {
                    (<any>Message).warning('['+this.tableForm.tableName+']表由系统保留，不允许创建');
                    return false;
                }

                let columnNameList: Array<string> = this.tableForm.columnInfos.map(a=>a.columnName);
                if(new Set(columnNameList).size !== this.tableForm.columnInfos.length){
                    (<any>Message).warning('存在重复列名');
                    return false;
                }

                for(let i=0; i<this.tableForm.columnInfos.length; i++) {
                    if(!this.tableForm.columnInfos[i].columnName) {
                        (<any>Message).warning('必须填写列名');
                        return false;
                    }

                    if(EnumUtils.isFeservedField(this.tableForm.columnInfos[i].columnName)) {
                        (<any>Message).warning('该列将由系统自动创建，不允许重复定义');
                        return false;
                    }

                    if(!this.tableForm.columnInfos[i].dataType) {
                        (<any>Message).warning('必须选择数据类型');
                        return false;
                    }
                }

                datasourceStore.buildTable(this.tableForm).then((tableInfo: any)=>{
                    this.tableForm.columnInfos = [];
                    this.tableForm.removedColumns = [];
                    if(datasourceStore.currentTablename) {
                        let tab: any = workspaceStore.tabPages.find(a => a && a.id && a.id == this.tableForm.tableName);
                        workspaceStore.remove(this.tableForm.tableName);
                        workspaceStore.tabPages.push(tab);
                    }
                    else {
                        workspaceStore.add({
                            id: tableInfo.tableName,
                            name: EnumUtils.getDisplayName(tableInfo.tableName),
                            type: EnumUtils.getType(tableInfo.tableName),
                            localhost: EnumUtils.getType(tableInfo.tableName) + "/" + EnumUtils.getDisplayName(tableInfo.tableName)
                        })

                        if(router.currentRoute.fullPath !== '/home/workspace') {
                            router.push('/home/workspace');
                        }
                    }
                    workspaceStore.select(tableInfo.tableName);

                    datasourceStore.load();
                });
            })
        }

        closeDialog() {
            datasourceStore.close();
        }

        inputRender(h:Function, index:number, key:string, value:any, type: string, isRemove: boolean, options?:Array<any>) {
            return h(CCInput, {
                props: {
                    value: value,
                    type: type,
                    attribute: key,
                    options: options,
                    disabled: isRemove,
                },
                on: {
                    change: (e:any)=>{
                        this.tableForm.columnInfos[index][e.attrName] = e.value;
                        if(this.tableForm.columnInfos[index].alterMode != GlobalVars.AlterMode.ADD) {
                            this.tableForm.columnInfos[index].alterMode = GlobalVars.AlterMode.CHANGE;
                        }
                    }
                }
            })
        }

        getColumns() {
            let result:Array<any> = [{
                key: 'columnName',
                title: '列名',
                render: (h: Function, params: any) :any => {
                    if(datasourceStore.currentTablename && params.row.alterMode!=GlobalVars.AlterMode.ADD) {
                        return h('span', params.row.columnName);
                    }
                    return this.inputRender(h, params.index, params.column.key, params.row.columnName, 'text', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                key: 'dataType',
                title: '类型',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.dataType.toLocaleUpperCase(), 'select', params.row.alterMode==GlobalVars.AlterMode.DROP, [
                        {text: "文本", value: "VARCHAR"},
                        {text: "内容", value:"TEXT"},
                        {text:"数字", value:"INT"},
                        {text:"时间", value:"DATETIME"},
                        {text:"金额", value:"DECIMAL"},
                        {text:"图片", value:"IMAGE"},
                        {text:"视频", value:"VIDEO"},
                        {text:"音频", value:"AUDIO"},
                        {text:"文档", value:"DOCUMENT"}
                    ]);
                }
            }, {
                key: 'defaultValue',
                title: '默认值',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.defaultValue, 'text', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                width: 60,
                align: 'center',
                key: 'canNotNull',
                title: '非空',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.canNotNull, 'checkbox', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                width: 60,
                align: 'center',
                key: 'unique',
                title: '唯一',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.unique, 'checkbox', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                width: 60,
                align: 'center',
                key: 'isAutoIncrement',
                title: '自增',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.isAutoIncrement, 'checkbox', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                width: 60,
                align: 'center',
                key: 'isPrimaryKey',
                title: '主键',
                render: (h: Function, params: any) :any => {
                    return this.inputRender(h, params.index, params.column.key, params.row.isPrimaryKey, 'checkbox', params.row.alterMode==GlobalVars.AlterMode.DROP);
                }
            }, {
                width: 60,
                align: 'center',
                title: '操作',
                render: (h:Function, params: any)=> {
                    return h(Button, {
                        props: {
                            icon: 'md-remove',
                            title: '移除列',
                            class: 'table-option-button'
                        },
                        style: {
                            width: '20px',
                            height: '20px',
                            lineHeight: '12px',
                            fontSize: '12px',
                            padding: '0px',
                        },
                        on: {
                            click: (e:any)=>{
                                params.row.alterMode = GlobalVars.AlterMode.DROP;
                                this.tableForm.removedColumns.push(params.row);
                                this.tableForm.columnInfos.splice(params.index, 1);
                            }
                        }
                    })
                }
            }];

            return result;
        }

        @Watch("datasourceForm.type")
        dbTypeChange(value: string) {
            switch(this.datasourceForm.type) {
                case 'org.mariadb.jdbc.Driver':
                case 'com.mysql.cj.jdbc.Driver':
                case 'com.mysql.jdbc.Driver':
                    this.datasourceForm.port = "3306";
                    break;
                case 'com.microsoft.sqlserver.jdbc.SQLServerDriver':
                    this.datasourceForm.port = "1433";
                    break;
                case 'org.postgresql.Driver':
                    this.datasourceForm.port = "5432";
                    break;
                case 'oracle.jdbc.OracleDriver':
                    this.datasourceForm.port = "1521";
                    break;
                case 'org.apache.derby.jdbc.ClientDriver':
                    this.datasourceForm.port = "1527";
                    break;
                case 'org.apache.hive.jdbc.HiveDriver':
                    this.datasourceForm.port = "9083";
                    break;
            }
        }

        saveSetting() {
            (<any>this.$refs.datasourceForm).validate((valid: any)=> {
                let datasource: any = {};
                if(this.datasourceForm.username) {
                    datasource.username = this.datasourceForm.username;
                }

                if(this.datasourceForm.password) {
                    datasource.password = this.datasourceForm.password;
                }

                if(this.datasourceForm.type) {
                    datasource.driver = this.datasourceForm.type;
                }

                switch(this.datasourceForm.type) {
                    case 'org.sqlite.JDBC':
                        datasource.url = 'jdbc:sqlite:' + this.datasourceForm.path;
                        break;
                    case 'com.mysql.jdbc.Driver':
                    case "com.mysql.cj.jdbc.Driver":
                        datasource.url = 'jdbc:mysql://' + this.datasourceForm.domain+':' + this.datasourceForm.port +
                            '/' + this.datasourceForm.dbName +
                            '?useUnicode=true&amp;characterEncoding=UTF-8&amp;useSSL=false&amp;autoReconnect=true&amp;'+
                            'failOverReadOnly=false&amp;serverTimezone=CTT';
                        break;
                    case 'org.mariadb.jdbc.Driver':
                        datasource.url = 'jdbc:mariadb://' + this.datasourceForm.domain+':' + this.datasourceForm.port +
                            '/' + this.datasourceForm.dbName +
                            '?useUnicode=true&amp;characterEncoding=UTF-8&amp;useSSL=false&amp;autoReconnect=true&amp;'+
                            'failOverReadOnly=false&amp;serverTimezone=CTT';
                        break;
                    case 'com.microsoft.sqlserver.jdbc.SQLServerDriver':
                        datasource.url = 'jdbc:sqlserver://' + this.datasourceForm.domain+':'+this.datasourceForm.port +
                            ';Database=' + this.datasourceForm.dbName;
                        break;
                    case 'org.postgresql.Driver':
                        datasource.url = 'jdbc:postgresql://' + this.datasourceForm.domain + ':' + this.datasourceForm.port +
                            '/' + this.datasourceForm.dbName;
                        break;
                    case 'oracle.jdbc.OracleDriver':
                        datasource.url = 'jdbc:oracle:thin:@' + this.datasourceForm.domain + ':' + this.datasourceForm.port +
                            ':' + this.datasourceForm.dbName;
                        break;
                    case 'org.apache.derby.jdbc.ClientDriver':
                        datasource.url = 'jdbc:derby://' + this.datasourceForm.domain + ':' + this.datasourceForm.port +
                            '/' + this.datasourceForm.dbName;
                        break;
                    case 'org.apache.hive.jdbc.HiveDriver':
                        datasource.url = 'jdbc:hive2://' + this.datasourceForm.domain + ':' + this.datasourceForm.port +
                            '/' + this.datasourceForm.dbName;
                        break;
                }

                datasource.default = true;

                datasourceStore.saveDatasource({dsName: this.datasourceForm.dsName, datasource: datasource});
            });
        }
    }
</script>

<template lang="pug" src="@/views/popup/addtablepopup.pug" />
<style scoped src="@/styles/popup/addtablepopup.css" />
