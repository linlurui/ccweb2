<script lang="ts">
    import {Component, Vue, Prop, Watch} from 'vue-property-decorator'
    import CCInput from "../common/ccinput/index.vue"
    import AppConfigTableStore from "../../store/modules/settings/AppConfigTableStore";
    import {getModule} from "vuex-module-decorators";
    import ProfileSettingStore from "../../store/modules/settings/ProfileSettingStore";

    const appConfigTableStore = getModule(AppConfigTableStore)
    const store = getModule(ProfileSettingStore)

    @Component({
        components: {
            'CCInput': CCInput
        }
    })
    export default class ProfileSetting extends Vue {
        @Prop() public value: string | undefined;

        private dictList: any[] = new Array();

        public defaultValue: string = '';

        mounted(): void {
            appConfigTableStore.load((config: any)=>{
                if(!config) {
                    return;
                }

                let keys:string[] = Object.keys(config);
                if(keys.indexOf('ccweb.dict.default') && config['ccweb.dict.default']) {
                    this.defaultValue = config['ccweb.dict.default'];
                }

                this.dictList = new Array();
                for(let i=0; i<keys.length; i++) {
                    this.dictList.push({
                        key: keys[i],
                        value: config[keys[i]],
                    })
                }
                if(this.dictList.length==0) {
                    this.dictList.push({
                        key: '',
                        value: '',
                    });
                }
            })
        }

        getKeyValuePartColumns() {
            return [
                {
                    title: '参数名',
                    slot: 'key',
                    render: (h: Function, params: any) :any => {
                        return h(CCInput, {
                            props: {
                                attribute: params.row.key + '.key',
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
                                attribute: params.row.key,
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

        public addDict() {
            let item: any = { key: '', value: '' };
            this.dictList.push(item);
        }

        public removeDict(index: number) {
            let item:any = this.dictList[index];
            let allKeys: string[] = Object.keys(appConfigTableStore.info);
            if(allKeys.indexOf(item.key)>-1) {
                store.markRemoved(item.key).finally(()=>{
                    this.dictList.splice(index, 1);
                })
            }
            else {
                this.dictList.splice(index, 1);
            }
        }

        changeDefaultValue(e: any) {
            if(!e.value || !e.value.trim()) {
                store.removedList.push('ccweb.dict.default');
                delete store.form['ccweb.dict.default'];
                return;
            }
            store.form['ccweb.dict.default'] = e.value;
        }

        changeValue(e: any) {
            if(!e.attrName) {
                return;
            }

            if(e.attrName.endsWith('.key')) {

                this.dictList[e.index].key = e.value;
                if(!/^[a-zA-Z0-9]+$/.test(e.value)) {
                    if(!e.value && e.old) {
                        store.markRemoved(e.old).finally(() => {
                            delete store.form[e.old];
                        })
                    }
                    this.$Message.warning('参数名只允许输入中英文、数字');
                    return;
                }

                if(e.value) {
                    store.form[e.value] = this.dictList[e.index].value;
                }

                if(e.old) {
                    store.markRemoved(e.old).finally(() => {
                        delete store.form[e.old];
                    })
                }
            }
            else {
                this.dictList[e.index].value = e.value;
                store.form[e.attrName] = e.value;
            }
        }
    }
</script>

<template lang="pug" src="@/views/settings/profileSetting.pug" />


<style>
    .platformField .ivu-input-wrapper,.platformField .ivu-select {
        max-width: 133px !important;
    }
    .ivu-form-item-content .ivu-btn {
        margin-left: 5px;
    }
    .platformTable .ivu-form-item-label {
        width: 100% !important;
        text-align: left !important;
    }
    .platformTable .ivu-table-wrapper {
        width: 100% !important;
    }
    .ivu-form-item-content {
        position: inherit !important;
    }
    .ivu-table td, .ivu-table th {
        min-width: 100px;
    }
</style>