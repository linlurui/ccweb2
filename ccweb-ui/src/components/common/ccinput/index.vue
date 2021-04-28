<script lang="ts">
    import {Component, Prop, Watch, Vue} from "vue-property-decorator"
    import {codemirror} from 'vue-codemirror'
    import "codemirror/theme/ambiance.css";
    require("codemirror/mode/javascript/javascript");

    @Component({
        components: {
            'codemirror': codemirror
        }
    })
    export default class CCInput extends Vue {

        constructor(arg: any) {
            super()
        }

        @Prop() public contentType: string | undefined;
        @Prop() public type: string | undefined;
        @Prop() public value: string | string[] | undefined;
        @Prop() public options: Array<any> | undefined;
        @Prop() public icon: string | undefined;
        @Prop() public attribute: string | undefined;
        @Prop() public width: string | undefined;
        @Prop() public disabled: boolean | undefined;
        @Prop() public readonly: boolean | undefined;
        @Prop() public className: string | undefined;
        @Prop() public title: string | undefined;
        @Prop() public max: number | undefined;
        @Prop() public min: number | undefined;
        @Prop() public index: number | undefined;
        @Prop() public css: any | undefined;
        @Prop() public placeholder: string | undefined;
        @Prop() public onSearch: Function | undefined;
        @Prop() public loading: boolean | undefined;
        @Prop() public onFocus: Function | undefined;
        @Prop() public base64: boolean | undefined;
        @Prop() public prepend: string | Array<any> | undefined;
        @Prop() public append: string | Array<any> | undefined;
        @Prop() public rows: number | undefined;
        @Prop() public defaultLabel: string | string[] | undefined;

        private prependValue = "";
        private appendValue = "";
        private newTagValue = "";
        private defaultColorList = [
            "primary",
            "success",
            "magenta",
            "red",
            "volcano",
            "orange",
            "gold",
            "yellow",
            "lime",
            "green",
            "cyan",
            "blue",
            "geekblue",
            "purple",
            "#FFA2D3",
            "#eb615d",
            "#e94a98",
            "#f3b047",
            "#76d252",
            "#5fc8c7",
            "#64aff7",
            "#7989f1",
            "#894ed8",
            "#a977e3"
        ];

        randomColor(){
            let num:number = Math.floor(Math.random()*(0 - 23) + 23);
            return this.defaultColorList[num];
        }

        public getDefaultLabel() {

            if(!this.options) {
                return '';
            }
            let item = this.options.find((a:any)=> a.value==this.value);
            if(!item) {
                return '';
            }

            if(item.text) {
                return item.text;
            }

            return item.value;
        }

        public search(e: any) {
            if(!this.onSearch) {
                return;
            }

            if(typeof(this.onSearch)!='function') {
                return;
            }

            this.onSearch(e);
        }

        public focus(e: any) {
            if(!this.onFocus) {
                return;
            }

            if(typeof(this.onFocus)!='function') {
                return;
            }

            this.onFocus(e);
        }

        public isArray(value: any) {
            if(!value) {
                return false;
            }

            if(Array.isArray(value)) {
                return true;
            }

            return false;
        }

        public getMaxWidth(value: string | Array<string> | undefined) {
            let len: number = 60;
            if(!value) {
                return len + 'px';
            }

            let fixed: number = 2;

            if(this.isString(value)) {
                len = (value.length * 12 + fixed);
            }

            else if(this.isArray(value)) {
                for(let i=0; i<value.length; i++) {
                    if(!value[i]) {
                        continue;
                    }

                    if((value[i].length * 12 + fixed) > len) {
                        len = (value[i].length * 12 + fixed);
                    }
                }
            }

            return len + 'px';
        }

        public isString(value: any) {
            if(!value) {
                return false;
            }

            if(typeof(value)=='string') {
                return true;
            }

            return false;
        }

        public getDefaultPrepend() {
            if(typeof(this.value)!='string' || !this.prepend ||
                !Array.isArray(this.prepend) || this.prepend.length<1) {
                return '';
            }

            if(this.prependValue) {
                return this.prependValue;
            }

            for(let i=0; i<this.prepend.length; i++) {
                if(!this.prepend[i]) {
                    continue;
                }

                if(typeof(this.prepend[i])=='object') {
                    if((<any>this.prepend)[i].value && (<string>this.value).startsWith((<any>this.prepend)[i].value)) {
                        this.prependValue = this.prepend[i];
                        return this.prepend[i];
                    }
                }
                else if(typeof(this.prepend[i])=='string') {
                    if((<string>this.value).startsWith(this.prepend[i])) {
                        this.prependValue = this.prepend[i];
                        return this.prepend[i];
                    }
                }
            }

            this.prependValue = this.prepend[0];
            return this.prepend[0];
        }

        public getDefaultAppend() {
            if(typeof(this.value)!='string' || !this.append ||
                !Array.isArray(this.append) || this.append.length<1) {
                return '';
            }

            if(this.appendValue) {
                return this.appendValue;
            }

            for(let i=0; i<this.append.length; i++) {
                if(!this.append[i]) {
                    continue;
                }

                if(typeof(this.append[i])=='object') {
                    if((<any>this.append)[i].value && (<string>this.value).endsWith((<any>this.append)[i].value)) {
                        this.appendValue = this.append[i];
                        return this.append[i];
                    }
                }
                else if(typeof(this.append[i])=='string') {
                    if((<string>this.value).endsWith(this.append[i])) {
                        this.appendValue = this.append[i];
                        return this.append[i];
                    }
                }
            }

            this.appendValue = this.append[0];
            return this.append[0];
        }

        public changePrependValue(value: string) {
            this.prependValue = value;
            this.changeValue(this.value);
        }

        public changeAppendValue(value: string) {
            this.prependValue = value;
            this.changeValue(this.value);
        }

        public changeValue(e: any) {
            if(e == undefined || e == null) {
                e = '';
            }
            if (typeof (e) == 'object' && e.target) {
                if(this.type == 'text') {
                    this.$emit('change', {attrName: this.attribute, value: this.prependValue + e.target.value + this.appendValue, old: this.value, index: this.index})
                    return;
                }
                this.$emit('change', {attrName: this.attribute, value: e.target.value, old: this.value, index: this.index})
            }

            else if(this.type == 'checkbox' && this.options) {
                let val = null;
                if(this.options.length==1 && this.options[0]) {
                    let obj: any = this.options[0];
                    val = obj.value;
                }
                this.$emit('change', {
                    attrName: this.attribute,
                    value: ((e && e.length>0) ? [val] : []),
                    old: ((this.value && this.value.length>0) ? [val] : [])
                    , index: this.index
                })
            }

            else if(this.type == 'checkbox') {
                this.$emit('change', {
                    attrName: this.attribute,
                    value: !this.value,
                    old: (this.value ? true : false)
                    , index: this.index
                })
            }

            else if(this.type == 'text'){
                this.$emit('change', {attrName: this.attribute, value: this.prependValue + e + this.appendValue, old: this.value, index: this.index})
            }

            else {
                this.$emit('change', {attrName: this.attribute, value: e, old: this.value, index: this.index})
            }
        }

        getValue() {

            debugger
            if(this.type=='checkbox' && typeof(this.value)!='boolean' && (!this.options || this.options.length<1)) {
                if(this.value=='true') {
                    return true;
                }
                else {
                    return false;
                }
            }

            if(typeof(this.value)=='string') {
                if (Array.isArray(this.prepend)) {
                    for(let i=0; i<this.prepend.length; i++) {
                        if(this.value.startsWith(this.prepend[i])) {
                            return this.value.substring(this.prepend[i].length);
                        }
                    }
                } else if (typeof (this.prepend) == 'string') {
                    if(this.value.startsWith(this.prepend)) {
                        return this.value.substring(this.prepend.length);
                    }
                }
            }

            return this.value;
        }

        public getColorHex(value: string): string {
            const regex = /^rgb\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)$/;
            if(regex.test(value)) {
                const result = regex.exec(value);
                if(!result) {
                    return value;
                }

                let list = new Array();
                list[0] = '#';
                list[1] = parseInt(result[1], 10).toString(16);
                list[2] = parseInt(result[2], 10).toString(16);
                list[3] = parseInt(result[3], 10).toString(16);

                return list.join('');
            }

            return value;
        }

        public getColorRgb(value: string) {
            const regex = /^#([\w\d]{2, 2})([\w\d]{2, 2})([\w\d]{2, 2})$/;
            if(regex.test(value)) {
                const result = regex.exec(value);
                if(!result) {
                    return value;
                }

                let list = new Array();
                list[0] = parseInt(result[1], 16)
                list[1] = parseInt(result[2], 16)
                list[2] = parseInt(result[3], 16)

                return 'rgb(' + list.join(',') + ')';
            }

            return value;
        }

        public openFileDialog() {
            // @ts-ignore
            document.getElementById(this.attribute + "_file_").click();
        }

        public getFile() {
            // @ts-ignore
            let input: any = document.getElementById(this.attribute + "_file_");

            if(input && input.files && input.files.length>0) {
                return input.files[0];
            }

            return null;
        }

        removeTag(event:any, name: string) {
            if(!this.value) {
                this.value = [];
            }
            else if(!Array.isArray(this.value)) {
                this.value = this.value.split(',');
            }
            const index:number = this.value.indexOf(name);
            (<any>this.value).splice(index, 1);

            this.changeValue((<Array<any>>this.value).join(','));
        }

        addTag() {
            if(!this.newTagValue) {
                return;
            }

            if(!this.value) {
                this.value = [];
            }
            else if(!Array.isArray(this.value)) {
                this.value = this.value.split(',');
            }

            let arr:Array<string> = this.newTagValue.split(";");
            for(let i=0; i<arr.length; i++) {
                if(!arr[i] || !arr[i].trim()) {
                    continue;
                }

                const index: number = this.value.indexOf(arr[i].trim());
                if (index > -1) {
                    return;
                }

                (<any>this.value).push(arr[i].trim());
            }
            this.newTagValue = '';

            this.changeValue((<Array<any>>this.value).join(','));
        }

        getTags() {
            if(!this.value) {
                return [];
            }

            if(Array.isArray(this.value)) {
                return this.value;
            }

            return this.value.split(',');
        }
    }
</script>

<style scoped src="./styles/ccinput.css" />
<template lang="pug" src="./views/ccinput.pug" />
<style>
    .ivu-tag-magenta .ivu-icon-ios-close,.ivu-tag-red .ivu-icon-ios-close,.ivu-tag-volcano .ivu-icon-ios-close,.ivu-tag-orange .ivu-icon-ios-close,.ivu-tag-gold .ivu-icon-ios-close,.ivu-tag-yellow .ivu-icon-ios-close,.ivu-tag-lime .ivu-icon-ios-close,.ivu-tag-green .ivu-icon-ios-close,.ivu-tag-cyan .ivu-icon-ios-close,.ivu-tag-blue .ivu-icon-ios-close,.ivu-tag-geekblue .ivu-icon-ios-close,.ivu-tag-purple .ivu-icon-ios-close {
        color: #666666 !important;
    }
</style>