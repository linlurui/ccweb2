<script lang="ts">
    import AddTablePopup from "@/components/popup/AddTablePopup.vue"
    import { Component, Vue, Prop } from 'vue-property-decorator';
    import WorkspaceStore from '@/store/modules/workspace/WorkspaceStore'
    import { getModule } from 'vuex-module-decorators'
    import EnumUtils from "@/common/utils/EnumUtils";
    import router from '@/router/.invoke/router'
    const workspaceStore = getModule(WorkspaceStore)
    import DatasourceStore from "@/components/common/cctable/store/DatasourceStore"
    const datasourceStore = getModule(DatasourceStore)
    import {GlobalVars} from "@/common/GlobalVars";
    import {ColumnInfo} from "@/components/common/cctable/store/CCTableStore";
    import LowDbUtils from "@/common/utils/LowDbUtils";
    import SystemTableUtils from "@/common/utils/SystemTableUtils";
    import PostUtils from "@/common/utils/PostUtils";
    import CCInput from "@/components/common/ccinput/index.vue";
    import WebConfigUtils from "@/common/utils/WebConfigUtils";

    @Component({
        components: {
            AddTablePopup,
            CCInput,
        }
    })
    export default class Navigation extends Vue {
        private testInfo: any = {
            table: '',
            type: '',
            columnInfos: [],
        }

        tab = 'data';
        private tableSearchKeyword: string = '';

        datasourceSetting() {
            datasourceStore.openDatasourceDialog()
        }

        showTestDialog(type: string) {
            this.testInfo.columnInfos = new Array();
            this.testInfo.table = '';
            this.testInfo.type = type;
            this.$store.state.WorkspaceStore.dialogs.showTestTable = true;
        }

        onSelectMenuItem(name: string) {
            if(!name) {
                return;
            }

            let page = this.$store.state.WorkspaceStore.tabPages.find((a: { id: string })=> (a && a.id && a.id == name));
            if(!page) {
                // pages中不存在该path时追加
                workspaceStore.add({
                    id: name,
                    name: EnumUtils.getDisplayName(name),
                    type: EnumUtils.getType(name),
                    localhost: EnumUtils.getType(name) + "/" + EnumUtils.getDisplayName(name),
                })
            }

            this.$store.state.WorkspaceStore.currentTab = name;
            this.$store.state.WorkspaceStore.currentType = EnumUtils.getType(name);

            if(router.currentRoute.fullPath !== '/home/workspace') {
                router.push('/home/workspace');
            }
        }

        tableFilter(value: string) {
            this.tableSearchKeyword = value;
        }


        getTables() {
            let tables: any[] = new Array();
            tables.push({text: '账号', value: 'user'});
            tables.push({text: '角色', value: 'role'});
            tables.push({text: '分组', value: 'group'});
            tables.push({text: '授权', value: 'userGroupRole'});
            for(let i=0; i<datasourceStore.tables.length; i++) {
                let displayName = (datasourceStore.tables[i].description
                    ? datasourceStore.tables[i].description
                    : datasourceStore.tables[i].tableName);
                tables.push({text: displayName, value: datasourceStore.tables[i].tableName});
            }

            return tables;
        }

        cancelTest() {
            this.$store.state.WorkspaceStore.dialogs.showTestTable=false;
            this.testInfo.columnInfos = new Array();
            this.testInfo.table = '';
            this.testInfo.type = '';
        }

        getTestColumns(){
            if(!this.testInfo.columnInfos) {
                return [];
            }
            return this.testInfo.columnInfos;
        }

        selectCurrent(row: any, index: number) {
            if(row._disabled) {
                return;
            }
            row._checked = !row._checked;
        }

        toColumnInfos(cols: Array<any>) : ColumnInfo[] {
            let result:Array<ColumnInfo> = new Array();
            if(!cols) {
                return [];
            }

            for(let i=0; i<cols.length; i++) {
                let dataType = cols[i].dataType.toLocaleUpperCase();
                let columnSetting:any = {
                    key: cols[i].columnName,
                    isPrimaryKey: cols[i].isPrimaryKey,
                    title: (cols[i].description || cols[i].columnName),
                    _disabled: false,
                    _checked: false,
                    ellipsis: false,
                    tooltip: cols[i].columnComment,
                    sortable: false,
                    fixed:'',
                    minWidth: 60,
                    value: '',
                    dontSearch: false,
                    type: 'selection',
                    dataType: dataType,
                };

                result.push(columnSetting);
            }

            return result;
        }

        choseTestTable(e: any) {
            if(!e.value) {
                return;
            }

            this.testInfo.table = e.value;

            datasourceStore.getServerColumns(e.value).then((data: any) => {
                if(!data || !data.columnInfos || !Array.isArray(data.columnInfos)) {
                    return;
                }

                this.testInfo.columnInfos = new Array();
                const localDb = LowDbUtils.use('ColumnsProfile')
                this.testInfo.columnInfos = localDb.getData(e.value);
                if(!this.testInfo.columnInfos && EnumUtils.isSystemTable(e.value)) {
                    this.testInfo.columnInfos = SystemTableUtils.getDefaultColumns(e.value);
                    localDb.setData(e.value, this.testInfo.columnInfos);
                }

                if(!this.testInfo.columnInfos || !this.testInfo.columnInfos.length || this.testInfo.columnInfos.length<1) {
                    this.testInfo.columnInfos = this.toColumnInfos(data.columnInfos);
                }
            })
        }

        testInterface() {
            let name:string = this.testInfo.table;
            if(!name) {
                return;
            }

            let columns = (<any>this.$refs).testColumns.getSelection();
            let matchHttpPort = document.URL.match(/httpPort=(\d+)/);
            let httpPort = matchHttpPort && matchHttpPort[1] ? matchHttpPort[1] : '80';
            let baseUrl = 'http://' + WebConfigUtils.getServer().domain + ':' + httpPort;
            let page = this.$store.state.WorkspaceStore.tabPages.find((a: { id: string })=> (a && a.id && a.id == name));
            if(!page) {
                let datasource = datasourceStore.apiConfig.entity.datasource.activated;
                // pages中不存在该path时追加
                workspaceStore.add({
                    id: name + '.' + this.testInfo.type,
                    name: EnumUtils.getDisplayName(this.testInfo.type) + "-" + EnumUtils.getDisplayName(name) + "[测试]",
                    type: GlobalVars.TAB_TYPE_INTERFACE,
                    localhost: EnumUtils.getType(name) + "/" + EnumUtils.getDisplayName(name),
                    json: {
                        method: PostUtils.getMethod(this.testInfo.type),
                        url: baseUrl + PostUtils.getUrl(datasource, this.testInfo.table, this.testInfo.type),
                        postData: PostUtils.getPostJson(this.testInfo.type, columns),
                        tips: PostUtils.getHelpText(this.testInfo.type)
                    }
                })
            }

            this.$store.state.WorkspaceStore.currentTab = name + '.' + this.testInfo.type;
            this.$store.state.WorkspaceStore.currentType = EnumUtils.getType(name);

            if(router.currentRoute.fullPath !== '/home/workspace') {
                router.push('/home/workspace');
            }
        }
    }
</script>

<template lang="pug" src="@/views/navigation.pug" />
<style scoped src="@/styles/navigation.css" />
