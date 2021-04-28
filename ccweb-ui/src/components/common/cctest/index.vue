<script lang="ts">
    import {Component, Prop, Vue, Watch} from 'vue-property-decorator'
    import CCInput from "@/components/common/ccinput/index.vue";
    import {getModule} from "vuex-module-decorators";
    import CCTestStore from "@/components/common/cctest/store/CCTestStore";
    import {TabPageInfo} from "@/store/modules/workspace/WorkspaceStore";

    const store = getModule(CCTestStore);

    @Component({
        components: {
            'CCInput': CCInput
        }
    })
    export default class CCTest extends Vue {

        @Prop() public id: string | undefined;
        @Prop() public mode: string | undefined;
        @Prop() public json: string | undefined;
        @Prop() public url: string | undefined;
        @Prop() public method: string | undefined;
        @Prop() public tips: string | undefined;

        public rules: any = {
            url: [
                { required: true, message: '请求地址不能为空', trigger: 'blur' },
                { type: 'string', message: '请输入正确的URL地址规则', pattern: /^https?:\/\/[a-zA-Z0-9_\u4e00-\u9fa5][a-zA-Z0-9_\-\u4e00-\u9fa5&\?=#\.\\\/;,:%～\+]+$/, trigger: 'blur' }
            ],
            param: [
                { type: 'string', message: '参数名只允许输入英文、数字、杠、点或下划线并以英文字母开头', pattern: /^[a-zA-Z][a-zA-Z0-9_\-\.]+$/, trigger: 'blur' }
            ],
        }

        public defaultForm: any = {
            id: this.id,
            url: this.url,
            params: [{
                key: '',
                value: '',
            }],
            paramJson: (this.json ? this.json : '{\n\t\n}'),
            headers: [{
                key: '',
                value: '',
            }],
            response: '',
            request: '',
            paramType: 'JSON',
            method: (this.method ? this.method : 'post'),
            loading: false,
            mode: this.mode,
        };

        mounted(): void {
            if(!this.id || this.id == 'default' || store.form[this.id]) {
                return;
            }

            store.form[this.id] = {
                id: this.id,
                url: this.url,
                params: [{
                    key: '',
                    value: '',
                }],
                paramJson: (this.json ? this.json : '{\n\t\n}'),
                headers: [{
                    key: '',
                    value: '',
                }],
                response: '',
                request: '',
                paramType: 'JSON',
                method: (this.method ? this.method : 'post'),
                loading: false,
                mode: this.mode,
            }
        }

        submit() {
            let tab: TabPageInfo = this.getCurrentTabPage();
            if(tab && tab.id) {
                store.post(tab.id);
            }
            else {
                store.post();
            }
        }

        hasTips() {
            if(this.tips && this.tips.trim()) {
                return true;
            }
            return false;
        }

        getTipList() {
            if(!this.tips) {
                return "";
            }

            let tipList:any[] = this.tips.split("\n");
            for(let i=0; i<tipList.length; i++) {
                if(!tipList[i]) {
                    continue;
                }

                if(/\s*\[Title\]/.test(tipList[i])) {
                    tipList[i] = {
                        type: 'title',
                        content: tipList[i].replace(/\s*\[Title\]/, "")
                    };
                }

                if(/\s*\[Code\]/.test(tipList[i])) {
                    tipList[i] = {
                        type: 'code',
                        content: tipList[i].replace(/\s*\[Code\]/, "")
                    };
                }

                if(/\s*\[Item\]/.test(tipList[i])) {
                    tipList[i] = {
                        type: 'item',
                        content: tipList[i].replace(/\s*\[Item\]/, "")
                    };
                }
            }

            return tipList;
        }

        getCurrentTabPage() {
            let selectedTab = this.$store.state.WorkspaceStore.currentTab;
            if(!selectedTab) {
                return null;
            }

            let page = this.$store.state.WorkspaceStore.tabPages.find((a:any)=> a && a.id && a.id == selectedTab);
            if(!page) {
                return null;
            }

            return page;
        }

        validate(attrName: string, value: string): string {
            if(!this.rules[attrName]) {
                return '';
            }

            let arr:Array<any> = this.rules[attrName].filter((a:any)=>a.required);
            if(arr && arr.length>0) {
                if(!value || !value.trim()) {
                    return arr[0].message;
                }
            }

            arr = this.rules[attrName].filter((a:any)=>a.min && a.min>0);
            if(arr && arr.length>0) {
                if(value.length<arr[0].min) {
                    return arr[0].message;
                }
            }

            arr = this.rules[attrName].filter((a:any)=>a.max && a.max>0);
            if(arr && arr.length>0) {
                if(value.length>arr[0].max) {
                    return arr[0].message;
                }
            }

            arr = this.rules[attrName].filter((a:any)=>a.pattern);
            if(arr && arr.length>0) {
                if(!arr[0].pattern.test(value)) {
                    return arr[0].message;
                }
            }

            if(value && value.trim() && attrName == 'paramJson') {
                try {
                    JSON.parse(value);
                }
                catch {
                  return '请输入正确的Json字符串';
                }
            }

            return '';
        }

        getParamsColumns(append: string) {
            return [
                {
                    title: '参数名',
                    slot: 'key',
                    render: (h: Function, params: any) :any => {
                        return h(CCInput, {
                            props: {
                                attribute: params.row.key + '.key.' + append,
                                ref: params.row.key + '.key',
                                type: 'text',
                                placeholder: '请输入参数名',
                                title: '参数名',
                                value: params.row.key,
                                index: params.index,
                            },
                            on: {
                                change: this.changeValue
                            }
                        })
                    }
                },{
                    title: '参数值',
                    slot: 'value',
                    render: (h: Function, params: any) :any => {
                        return h(CCInput, {
                            props: {
                                attribute: params.row.key + '.value.' + append,
                                ref: params.row.key,
                                type: 'text',
                                placeholder: '请输入参数值',
                                title: '参数值',
                                value: params.row.value,
                                index: params.index,
                            },
                            on: {
                                change: this.changeValue
                            }
                        })
                    }
                }, {
                    title: '操作', slot: 'action', width: 120
                }
            ];
        }

        public addParam() {
            let item: any = { key: '', value: '' };
            // @ts-ignore
            store.form[this.id].params.push(item);
        }

        public removeParam(index: number) {
            // @ts-ignore
            store.form[this.id].params.splice(index, 1);
        }

        public addHeader() {
            let item: any = { key: '', value: '' };
            // @ts-ignore
            store.form[this.id].headers.push(item);
        }

        public removeHeader(index: number) {
            // @ts-ignore
            store.form[this.id].headers.splice(index, 1);
        }

        getForm() {
            if(!this.id || !store.form || !store.form[this.id]) {
                return {
                    id: this.id,
                    url: this.url,
                    params: [{
                        key: '',
                        value: '',
                    }],
                    paramJson: (this.json ? this.json : '{\n\t\n}'),
                    headers: [{
                        key: '',
                        value: '',
                    }],
                    response: '',
                    request: '',
                    paramType: 'JSON',
                    method: (this.method ? this.method : 'post'),
                    loading: false,
                    mode: this.mode,
                }
            }

            return store.form[this.id];
        }

        getId() {
            if(!this.id) {
                return 'default';
            }

            return this.id;
        }

        changeValue(e: any) {

            if(!this.id || !store.form) {
                return;
            }

            if(!store.form[this.id]) {
                store.form[this.id] = {};
            }

            if(e.attrName == 'url') {
                let pattern: RegExp = /^(post|put|delete|get|options|patch)(post|put|delete|get|options|patch)*([\w\W]*)$/;
                store.form[this.id].method = e.value.replace(pattern, '$1');
                e.value = e.value.replace(pattern, '$3');
            }

            let el:any = (<any>this.$refs)[e.attrName];
            if(el) {
                if(e.attrName.endsWith('.key.headers') || e.attrName.endsWith('.key.params')) {
                    el.error = this.validate('param', e.value);
                }
                else {
                    el.error = this.validate(e.attrName, e.value);
                }

                if(el.error) {
                    return;
                }
            }

            try {
                if (e.attrName.endsWith('.key.headers')) {
                    store.form[this.id].headers[e.index].key = e.value;
                } else if (e.attrName.endsWith('.value.headers')) {
                    store.form[this.id].headers[e.index].value = e.value;
                } else if (e.attrName.endsWith('.key.params')) {
                    store.form[this.id].params[e.index].key = e.value;
                } else if (e.attrName.endsWith('.value.params')) {
                    store.form[this.id].params[e.index].value = e.value;
                } else {
                    store.form[this.id][e.attrName] = e.value;
                }
            }
            catch (e) {
                this.$Message.error(e.message);
            }
        }
    }
</script>

<template lang="pug" src="./views/cctest.pug" />
<style>
    .cctest {
        display: block;
        overflow: auto;
        height: calc(100vh - 166px);
    }
    .platformTable .ivu-form-item-label {
        width: 100% !important;
        text-align: left !important;
    }
    .platformTable .ivu-table-wrapper {
        width: 100% !important;
    }
    .ivu-input-wrapper textarea {
        height: 100% !important;
        overflow-y: auto !important;
    }
    .ivu-form-item-error-tip {
        position: absolute;
        top: auto !important;
        left: auto !important;
    }
</style>