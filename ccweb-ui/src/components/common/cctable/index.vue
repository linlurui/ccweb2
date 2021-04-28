<script lang="ts">
    import {Component, Prop, Vue} from 'vue-property-decorator'
    import CCTableStore, {
        ColumnInfo, ConditionInfo, ListTargetInfo, OptionInfo
    } from "@/components/common/cctable/store/CCTableStore"
    import {getModule} from "vuex-module-decorators"
    import LowDbUtils from "@/common/utils/LowDbUtils"
    import CCInput from "@/components/common/ccinput/index.vue"
    import {Message} from "iview";
    import DatasourceStore from '@/components/common/cctable/store/DatasourceStore'
    import EnumUtils from "@/common/utils/EnumUtils";
    import HttpUtils from "@/common/utils/HttpUtils";
    import CCViewer from "@/components/common/ccviewer/index.vue";
    import axios from 'axios'
    import SystemTableUtils from "@/common/utils/SystemTableUtils";
    const store = getModule(CCTableStore);

    @Component({
        components: {
            "CCInput" : CCInput,
        }
    })
    export default class CCTable extends Vue {

        constructor() {
            super()
        }

        public displayColumns: ColumnInfo[] = [];
        public searchColumns: ColumnInfo[] = [];
        public editColumns: ColumnInfo[] = [];
        public serverColumns: Array<any> = [];
        private datasourceStore:DatasourceStore = getModule(DatasourceStore);
        private columns: Array<ColumnInfo> = new Array();

        private options: Array<OptionInfo> = new Array();
        private datalist: Array<any> = new Array();
        private height: number = 400;
        private hasEdit: boolean = false;
        private editItem: any = {};
        private currentEditId: string = "";
        private modalTitle: string = "";
        private hasDelete:boolean = false;
        public loading:boolean = true;
        public total: number = 0;

        @Prop() public table: string | undefined;
        @Prop() public pageSize: number | undefined;
        @Prop() public pageIndex: number | undefined;

        mounted(): void {
            this.readyColumns(()=>{
                if (!this.columns || !this.columns.length) {
                    return;
                }

                this.options = [];
                this.search()
            });
        }

        changePageNum(value: number) {
            this.pageIndex = value;
            this.search();
        }

        changePageSize(value: number) {
            this.pageSize = value;
            this.search();
        }

        loadOptions(isOpen: boolean, column: ColumnInfo) {
            if(isOpen == false) {
                this.options = [];
            }
            else if(!column.options || !column.options.length || column.options.length<1) {
                if(column.optionsFilter instanceof Function) {
                    column.optionsFilter();
                }
            }
        }

        clickRow(row: any, index: number) {
            if(row._disabled) {
                return;
            }
            row._checked = !row._checked;
        }

        dblClickRow(row: any, index: number) {
            this.onEdit();
        }

        onChangeFormValue(e: any) {
            if(e.value instanceof File) {
                HttpUtils.upload(this.table + '/' + e.attrName + '/upload', e.value).then((reason: any)=>{
                    this.$set(this.editItem, e.attrName, reason.file);
                })
                return;
            }
            this.$set(this.editItem, e.attrName, e.value);
        }

        onAdd() {
            this.hasEdit = true;
            this.editItem = {};
            this.modalTitle = "新增";
            this.currentEditId = "";
            //this.datalist.splice(0, 0, {_isNew: true, _checked:true, _disabled: true});
        }

        onEdit() {
            let selection:Array<any> = (<any>this.$refs.selection).getSelection();
            if(!selection || selection.length<1) {
                (<any>Message).warning('没有选中的数据');
                return;
            }

            if(selection.length>1) {
                (<any>Message).warning('不能同时编辑多行');
                return;
            }

            let cols = this.columns.filter(a=>a.isPrimaryKey);
            if(!cols || cols.length<1) {
                (<any>Message).warning('找不到主键');
                return;
            }

            this.modalTitle = "修改";
            let primaryKey:string = cols[0].key;
            this.currentEditId = selection[0][primaryKey];

            for(let i=0; i<this.columns.length; i++) {
                if(this.columns[i].type=='search' && this.columns[i].options && this.columns[i].key && selection[0][this.columns[i].key]) {
                    // @ts-ignore
                    let item:any = this.columns[i].options.find((a: any)=> a.value==selection[0][this.columns[i].key]);
                    if(item && item.text) {
                        this.columns[i].defaultLabel = item.text;
                    }
                }
            }

            this.editItem = selection[0];
            this.hasEdit = true;
        }

        onSubmit() {
            if(!this.editItem) {
                (<any>Message).warning('没有需要提交的数据');
            }

            if(this.currentEditId) {
                store.update({table: this.table, id: this.currentEditId, data: this.editItem}).then(()=>{
                    this.search();
                });
            }

            else {;
                store.add({table: this.table, data: [this.editItem]}).then(()=>{
                    this.search();
                });
            }
        }

        preRemove() {
            let selection:Array<any> = (<any>this.$refs.selection).getSelection();
            if(!selection || selection.length<1) {
                (<any>Message).warning('没有选中的数据');
                return;
            }

            this.hasDelete=true
        }

        onRemove() {
            let selection:Array<any> = (<any>this.$refs.selection).getSelection();
            if(!selection || selection.length<1) {
                (<any>Message).warning('没有选中的数据');
                return;
            }

            this.modalTitle = "删除";
            let deleteIds:Array<number> = new Array();
            for(let i=0; i<selection.length; i++) {
                let cols = this.columns.filter(a=>a.isPrimaryKey);
                if(!cols || cols.length<1) {
                    continue;
                }
                deleteIds.push(selection[i][cols[0].key]);
            }

            if(deleteIds) {
                store.remove({table: this.table, idlist: deleteIds}).then(()=>{
                    this.search();
                });
            }
        }

        async readyColumns(callback: Function) {
            const localDb = LowDbUtils.use('ColumnsProfile')
            if(!this.columns || this.columns.length<1) {
                this.columns = localDb.getData(this.table);
                if(!this.columns && EnumUtils.isSystemTable(this.table)) {
                    this.columns = SystemTableUtils.getDefaultColumns(this.table);
                    localDb.setData(this.table, this.columns);
                }
            }

            if(!this.columns || !this.columns.length || this.columns.length<1) {
                await this.datasourceStore.getServerColumns(this.table).then((data: any) => {
                    if(!data || !Array.isArray(data.columnInfos) || data.columnInfos.length<1) {
                        (<any>Message).warning("列配置丢失，请删除该表重新创建");
                        return;
                    }
                    this.serverColumns = data.columnInfos;
                    this.columns = this.converToColumnInfos(this.serverColumns, this.table);
                });
            }

            if(!this.columns || !this.columns.length || this.columns.length<1) {
                return
            }

            for(let i=0; i<this.columns.length; i++) {
                if(!this.columns[i]) {
                    continue;
                }

                let filter = ()=> {
                    this.columns[i].loading = true;
                    store.filterOptions(this.columns[i].listTarget).then((result:any)=> {
                        this.fillOptions(result, this.columns[i]);
                        this.columns[i].loading = false;
                    })
                }

                this.columns[i].optionsFilter = filter;
                await filter();
            }

            if(!this.displayColumns || this.displayColumns.length<1) {
                this.displayColumns = this.getDisplayColumns();
            }

            if(!this.searchColumns || this.searchColumns.length<1) {
                this.searchColumns = this.getSearchColumns();
            }

            if(!this.editColumns || this.editColumns.length<1) {
                this.editColumns = this.getEditColumns();
            }

            if(callback) {
                callback();
            }
        }

        converToColumnInfos(cols: Array<any>, table?: string) : ColumnInfo[] {
            let result:Array<ColumnInfo> = new Array();
            if(!cols) {
                return [];
            }

            for(let i=0; i<cols.length; i++) {
                let dataType = cols[i].dataType.toLocaleUpperCase();
                let type:'text' | 'select' | 'checkbox' | 'date' | 'number' | 'search' | 'decimal' | 'file' = 'text';
                switch(dataType) {
                    case 'DATETIME':
                        type = 'date';
                        break;
                    case 'DECIMAL':
                        type = 'decimal';
                        break;
                    case 'INT':
                    case 'TINYINT':
                    case 'BIGINT':
                        type = 'number';
                        break;
                    case 'IMAGE':
                    case 'VIDEO':
                    case 'AUDIO':
                    case 'DOCUMENT':
                        type = 'file';
                        break;
                    case 'LABEL':
                        type = 'select';
                        break
                }

                let canEdit:boolean = (!cols[i].isPrimaryKey)
                if(EnumUtils.isFeservedField(cols[i].columnName)) {
                    canEdit = false;
                }
                let columnSetting:any = {
                    key: cols[i].columnName,
                    isPrimaryKey: cols[i].isPrimaryKey,
                    title: (cols[i].description || cols[i].columnName),
                    display: true,
                    disabled: false,
                    canEdit: canEdit,
                    ellipsis: false,
                    tooltip: cols[i].columnComment,
                    sortable: false,
                    fixed:'',
                    minWidth: 60,
                    value: '',
                    dontSearch: false,
                    type: type,
                    dataType: dataType,
                };

                if(dataType=='LABEL') {
                    columnSetting.listTarget = {
                        table: table, key: columnSetting.key, value: columnSetting.key
                    }
                    columnSetting.type = 'select';
                }
                else if(cols[i].columnName=='userId' || cols[i].columnName=='createBy' || cols[i].columnName=='modifyBy') {
                    columnSetting.listTarget = {
                        table: "user", key: "username", value: "userId"
                    }
                    columnSetting.type = 'search';
                }

                else if(cols[i].columnName=='roleId') {
                    columnSetting.listTarget = {
                        table: "role", key: "roleName", value: "roleId"
                    }
                    columnSetting.type = 'search';
                }

                else if(cols[i].columnName=='groupId') {
                    columnSetting.listTarget = {
                        table: "group", key: "groupName", value: "groupId"
                    }
                    columnSetting.type = 'search';
                }

                if(dataType=="IMAGE") {
                    columnSetting.options = ['.jpg', '.jpeg', '.png', '.gif'];
                }

                else if(dataType=="VIDEO") {
                    columnSetting.options = ['.mp4', '.webm', '.ogg'];
                }

                else if(dataType=="AUDIO") {
                    columnSetting.options = ['.mp3', '.wav', '.ogg'];
                }

                else if(dataType=="DOCUMENT") {
                    columnSetting.options = ['.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.pdf'];
                }

                result.push(columnSetting);
            }

            return result;
        }

        getSearchColumns() {
            if(!this.columns) {
                return [];
            }

            let result:ColumnInfo[] = this.columns.filter(a=> !a.dontSearch && a.type != "file");

            return result;
        }

        getDisplayColumns() {
            let result:Array<any> = [{
                type: 'selection',
                width: 60,
                align: 'center',
                fixed: 'left'
            }];

            if(!this.columns || !this.columns.length) {
                return [];
            }

            let cols = this.columns.filter((a:ColumnInfo)=> a.display);
            for(let i=0; i<cols.length; i++) {
                cols[i].render = (h:Function, p:any) => {
                    let value = p.row[cols[i].key];
                    if(cols[i].type == 'checkbox') {
                        if(value) {
                            value = '是';
                        }
                        else {
                            value = '否';
                        }
                    }

                    else if(cols[i].type == "select" || cols[i].type == "search") {
                        // @ts-ignore
                        let optionText:string = this.getOptionText(value, cols[i].options);
                        // @ts-ignore
                        if(optionText=="-" && cols[i].listTarget) {
                            // @ts-ignore
                            optionText = this.getOptionDictText(value, cols[i].listTarget, cols[i]);
                        }

                        value = optionText;
                    }

                    else if(cols[i].type == "file") {
                        let pkColumn:any = cols.find(a=>a.isPrimaryKey);
                        if(pkColumn) {
                            let id = p.row[pkColumn.key];
                            return h(CCViewer, {
                                props: {
                                    id: this.table + '_' + cols[i].key,
                                    type: cols[i].dataType,
                                    srcList: [this.getSrc(cols[i].key, id, this.getSuffix(value), cols[i].dataType)],
                                    serverPageTurning: this.isServerPageTurning(value),
                                    suffix: this.getSuffix(value),
                                    convertType: (this.getSuffix(value)=='DOC' ? 'html' : undefined),
                                    pageNum: 1
                                }
                            });
                        }
                    }

                    return h('span', value);
                }

                result.push(cols[i]);
            }

            return result;
        }

        getSrc(fieldName: string, id: string, suffix:string, dataType?: string) {
            switch (dataType) {
                case "IMAGE":
                    return axios.defaults.baseURL + "/download/" + this.table + "/" + fieldName + "/" + id;
                case "VIDEO":
                case "AUDIO":
                    return axios.defaults.baseURL + "/play/" + this.table + "/" + fieldName + "/" + id;
                default:
                    if(suffix=='DOCX' || suffix=='XLS' || suffix=='XLSX') {
                        return axios.defaults.baseURL + "/download/" + this.table + "/" + fieldName + "/" + id;
                    }
                    return axios.defaults.baseURL + "/preview/" + this.table + "/" + fieldName + "/" + id;
            }
        }

        isServerPageTurning(value: string) {
            switch(this.getSuffix(value)) {
                case "PDF":
                case "PPT":
                case "PPTX":
                    return true;
            }

            return false;
        }

        getEditColumns() {

            if(!this.columns || !this.columns.length) {
                return [];
            }

            let cols = this.columns.filter((a:ColumnInfo)=> a.canEdit);

            return cols;
        }

        onChangeSearchCondition(e: any) {
            if(!this.searchColumns ||
                !this.searchColumns.length ||
                this.searchColumns.length<1) {
                return;
            }

            this.searchColumns
                .filter((item: ColumnInfo)=>{item.key==e.attrName})
                .forEach((column: ColumnInfo)=>{
                column.value = e.value;
            });
        }

        search() {
            this.loading = true;
            let conditions: Array<ConditionInfo> = this.searchColumns
                .filter((item: ColumnInfo)=>{ item.value || item.value=="0" })
                .map((column: ColumnInfo)=>{
                    let condition:ConditionInfo = {
                        name: column.key,
                        value: column.value,
                        algorithm: (column.type=="text" ? "LIKE" : "EQ")
                    };

                    return condition;
                });

            store.count({table: this.table, conditions: conditions}).then((data: any)=>{
                this.total = data;
            })

            store.search({table: this.table, pageIndex: this.pageIndex, pageSize: this.pageSize, conditions: conditions}).then((data: Array<any>)=>{
                if(!data) {
                    return;
                }

                this.datalist = data;
            }).finally(()=>{
                this.loading = false;
            });
        }

        public getSuffix(value: string) {
            if(!value) {
                return '';
            }

            let arr = value.split('.');
            if(!arr || !arr.length) {
                return '';
            }

            let suffix:string = arr[arr.length-1].toLocaleUpperCase();

            return suffix;
        }

        private getOptionDictText(data: any, listTarget?: ListTargetInfo, column?: ColumnInfo) {
            let defaultText = "-";
            if(!listTarget || !listTarget.table) {
                return defaultText;
            }

            if(!listTarget.key || !listTarget.value) {
                return defaultText;
            }

            let dictOptions:Array<any> = store.optionsDist[listTarget.table];
            if(!dictOptions || dictOptions.length<1) {
                return defaultText;
            }

            for(let i=0; i<dictOptions.length; i++) {
                if(dictOptions[i] && dictOptions[i][listTarget.value]==data) {
                    return dictOptions[i][listTarget.key];
                }
            }

            return defaultText;
        }

        private getOptionText(data: any, options?: Array<OptionInfo>) {
            let defaultText = "-";
            if(!options) {
                return defaultText;
            }

            for(let k=0; k<options.length; k++) {
                if(!options[k] || options[k]["value"]!=data) {
                    continue;
                }

                if(options[k]["text"]) {
                    return options[k]["text"];
                }
            }

            return defaultText;
        }

        private fillOptions(result: Array<any>, column: ColumnInfo) {
            if(!result || !result.length || result.length<1) {
                return;
            }

            let opts: Array<OptionInfo> = new Array<OptionInfo>();
            for(let j=0; j<result.length; j++) {
                if(!result || !result[j]) {
                    continue;
                }
                let item:OptionInfo = {};

                // @ts-ignore
                item.text = result[j][column.listTarget.key];
                // @ts-ignore
                item.value = result[j][column.listTarget.value];

                opts.push(item);
            }
            this.options = opts;
            column.options = opts;
        }
    }
</script>

<style scoped src="./styles/cctable.css" />
<template lang="pug" src="./views/cctable.pug" />