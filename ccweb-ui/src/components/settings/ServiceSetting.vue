<script lang="ts">
    import {Component, Vue, Prop, Watch} from 'vue-property-decorator'
    import CCInput from "../common/ccinput/index.vue"
    import {getModule} from "vuex-module-decorators"
    import AppConfigTableStore from "../../store/modules/settings/AppConfigTableStore"
    import WebConfigUtils from "@/common/utils/WebConfigUtils";
    const appConfigTableStore:AppConfigTableStore = getModule(AppConfigTableStore)

    @Component({
        components: {
            'CCInput': CCInput
        }
    })
    export default class ServiceSetting extends Vue {
        @Prop() public value: string | undefined;

        public rules: any = {
            serviceName: [
                {required: true, message: '服务名称不能为空', trigger: 'blur'},
                {max: 20, message: '不能超过20个字符', trigger: 'blur'},
                {min: 5, message: '不能少于5个字符', trigger: 'blur'},
                {
                    type: 'string',
                    message: '只允许输入英文、数字、杠、点或下划线并以英文字母开头',
                    pattern: /^[a-zA-Z][a-zA-Z0-9_\-\.]+$/,
                    trigger: 'blur'
                }
            ],
            admin: [
                {required: true, message: '管理员账号不能为空', trigger: 'blur'},
                {max: 16, message: '不能超过16个字符', trigger: 'blur'},
                {
                    type: 'string',
                    message: '只允许输入中英文、数字或下划线',
                    pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
                    trigger: 'blur'
                }
            ],
            password: [
                {required: true, message: '管理员密码不能为空', trigger: 'blur'},
                {min: 5, message: '不能少于5个字符', trigger: 'blur'},
                {max: 20, message: '不能超过20个字符', trigger: 'blur'}
            ],
            accessIp: [
                {
                    type: 'string',
                    message: '请输入正确IP地址或域名',
                    pattern: /^((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])|[\d\w][\d\w\-_\.]+[\d\w])$/,
                    trigger: 'blur'
                }
            ],
            forwardFrom: [
                {
                    type: 'string',
                    message: '请输入正确的URL地址规则',
                    pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5][a-zA-Z0-9_\-\u4e00-\u9fa5&\?=#\.\\\/;,:%～\+]+$/,
                    trigger: 'blur'
                }
            ],
            forwardTo: [
                {
                    type: 'string',
                    message: '请输入正确IP地址或域名',
                    pattern: /^https?:\/\/((\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])|[\d\w][\d\w\-_\.]+[\d\w])(:\d+)?$/,
                    trigger: 'blur'
                }
            ]
        }

        public form: any = {
            accessIp: '',
            serviceName: '',
            admin: '',
            password: '',
            defaultZone: '',
            defaultZoneProtocol: '',
            accessTime: '',
            md5PublicKey: '',
            aesPublicKey: '',
            whiteList: [],
            blackList: [],
            mimeTypes: [],
            uploadPath: '',
            multiple: false,
            maxSize: 0,
            scalRatio: 0,
            fixedWidth: 0,
            watermark: '',
            forwardFrom: '',
            forwardTo: '',
            forwardToProtocol: '',
            forwardTable: [],
        }

        public editForward: any = {
            from: '',
            to: '',
            index: -1,
        };

        private keySet: any = {
            serviceName: 'spring.application.name',
            admin: 'ccweb.security.admin.username',
            password: 'ccweb.security.admin.password',
            md5PublicKey: 'ccweb.security.encrypt.MD5.publicKey',
            aesPublicKey: 'ccweb.security.encrypt.AES.publicKey',
            whiteList: 'ccweb.ip.whiteList',
            blackList: 'ccweb.ip.blackList',
            limitTimeBegin: 'ccweb.limitTime.begin',
            limitTimeEnd: 'ccweb.limitTime.end',
            uploadPath: 'ccweb.upload.basePath',
            mimeTypes: 'ccweb.upload.mimeTypes',
            multiple: 'ccweb.upload.multiple',
            maxSize: 'ccweb.upload.maxSize',
            scalRatio: 'ccweb.download.thumb.scalRatio',
            fixedWidth: 'ccweb.download.thumb.fixedWidth',
            watermark: 'ccweb.download.thumb.watermark',
            defaultZone: 'eureka.client.serviceUrl.defaultZone'
        }
        
        public httpPort: string;
        private httpsPort: string;

        constructor() {
            super()
            let matchHttpPort = document.URL.match(/httpPort=(\d+)/);
            this.httpPort = matchHttpPort && matchHttpPort[1] ? matchHttpPort[1] : '8080';

            let matchHttpsPort = document.URL.match(/httpsPort=(\d+)/);
            this.httpsPort = matchHttpsPort && matchHttpsPort[1] ? matchHttpsPort[1] : '8088';

            let logPath = 'service/ccweb.log';
            if (process.platform != 'win32' && !WebConfigUtils.getProduction()) {
                logPath = 'dist/' + logPath;
            }
            logPath = './' + logPath;

            const child:any = eval("require('child_process')");
            child.spawn('tail' , ['-f' , logPath]).stdout.on('data' , (data: any)=> {
                let line = data.toString('utf-8')
                let ele: any = document.getElementById('ccweb-log');
                if(!ele) {
                    return;
                }

                if(line) {
                    ele.innerText += line.trim();
                }
                ele.scrollTop = ele.scrollHeight;
            })
        }

        mounted(): void {
            appConfigTableStore.load((config: any)=>{
                this.init(config);
            })
        }

        changeValue(e: any) {
            let value: string = '';
            if(e.attrName=='accessTime') {
                value = e.value[0] + '-' + e.value[1]
            }
            else {
                value = e.value;
            }
            let el:any = (<any>this.$refs)[e.attrName];
            if(!el) {
                return;
            }
            if(e.attrName=='forwardFrom' || e.attrName=='forwardTo') {
                el = (<any>this.$refs)['forward'];
            }
            el.error = this.validate(e.attrName, value);
            if(!el.error) {
                if(e.attrName=='accessIp' &&
                    this.form.whiteList.findIndex((a: any)=>a.key==value)==-1) {
                    this.form.accessIp = value;
                }
                else if(e.attrName=='accessTime') {
                    appConfigTableStore.set({key:'ccweb.limitTime.begin', value: e.value[0]});
                    appConfigTableStore.set({key: 'ccweb.limitTime.end', value: e.value[1]});
                }
                else {
                    if(e.attrName=='defaultZone') {
                        let arr: Array<string> = e.value.split("://");
                        if(arr && arr.length==2) {
                            this.form.defaultZone = arr[1];
                            this.form.defaultZoneProtocol = arr[0] + "://";
                        }
                    }
                    else if(e.attrName=='forwardTo') {
                        let arr: Array<string> = e.value.split("://");
                        if(arr && arr.length==2) {
                            this.form.forwardTo = arr[1];
                            this.form.forwardToProtocol = arr[0] + "://";
                        }
                    }
                    else {
                        this.form[e.attrName] = e.value;
                    }

                    let key: string = this.keySet[e.attrName];
                    if(key) {
                        appConfigTableStore.set({key: key, value: e.value});
                    }
                }
            }
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

            return '';
        }

        addAccessIp() {
            if(!this.form.accessIp || this.form.whiteList.findIndex((a: any)=>a.key==this.form.accessIp)>-1) {
                return;
            }
            this.form.whiteList.push({ "key": this.form.accessIp, "label": this.form.accessIp, "disabled": false });

            let list:Array<string> = [];
            for(let i=0; i<this.form.whiteList.length; i++) {
                if(this.form.blackList.indexOf(this.form.whiteList[i].key)>-1) {
                    list.push(this.form.whiteList[i].key);
                }
            }
            appConfigTableStore.set({key: "whiteList", value: list.join(',')});
            appConfigTableStore.set({key: "blackList", value: this.form.blackList.join(',')});
        }

        onTransfer(targetKeys:Array<string>, direction:string, moveKeys:Array<string>) {
            if(!moveKeys) {
                return;
            }

            for(let i=0; i<moveKeys.length; i++) {
                let index = this.form.blackList.indexOf(moveKeys[i]);
                if(direction=='right') {
                    if(index>-1) {
                        continue;
                    }

                    this.form.blackList.push(moveKeys[i]);
                }
                else {
                    if(index==-1) {
                        continue;
                    }

                    this.form.blackList.splice(index, 1);
                }
            }

            let list:Array<string> = [];
            for(let i=0; i<this.form.whiteList.length; i++) {
                if(this.form.blackList.indexOf(this.form.whiteList[i].key)>-1) {
                    list.push(this.form.whiteList[i].key);
                }
            }
            appConfigTableStore.set({key: "whiteList", value: list.join(',')});
            appConfigTableStore.set({key: "blackList", value: this.form.blackList.join(',')});
        }

        pickFolder() {
            let openFolder:Function = (files: Array<string>) => {
                if(!files || !files.length || files.length!=1) {
                    return;
                }
                this.changeValue({attrName:'uploadPath', value: files[0], old:this.form.uploadPath});
            }

            eval("require('electron').remote.dialog.showOpenDialog({properties:['openDirectory']}, openFolder)");
        }

        private init(config: any) {
            if(!this.form) {
                this.form = {
                    accessIp: '',
                    serviceName: '',
                    admin: '',
                    password: '',
                    defaultZone: '',
                    defaultZoneProtocol: '',
                    accessTime: '',
                    md5PublicKey: '',
                    aesPublicKey: '',
                    whiteList: [],
                    blackList: [],
                    mimeTypes: [],
                    uploadPath: '',
                    multiple: false,
                    maxSize: 0,
                    scalRatio: 0,
                    fixedWidth: 0,
                    watermark: '',
                    forwardFrom: '',
                    forwardTo: '',
                    forwardToProtocol: '',
                    forwardTable: [],
                }
            }

            this.form.serviceName = config['spring.application.name'];
            this.form.admin = config['ccweb.security.admin.username'];
            this.form.password = config['ccweb.security.admin.password'];
            if(config['eureka.client.serviceUrl.defaultZone']) {
                let arr: Array<string> = config['eureka.client.serviceUrl.defaultZone'].split("://");
                if(arr && arr.length==2) {
                    this.form.defaultZone = arr[1];
                    this.form.defaultZoneProtocol = arr[0] + "://";
                }
            }
            this.form.md5PublicKey = config['ccweb.security.encrypt.MD5.publicKey'];
            this.form.aesPublicKey = config['ccweb.security.encrypt.AES.publicKey'];
            if(config['ccweb.ip.whiteList']) {
                this.form.whiteList = [];
                let list:Array<string> = config['ccweb.ip.whiteList'].split(',');
                for(let i=0; i<list.length; i++) {
                    this.form.whiteList.push({ "key": list[i], "label": list[i], "disabled": false });
                }
            }
            if(config['ccweb.ip.blackList']) {
                this.form.blackList = config['ccweb.ip.blackList'].split(',');
            }

            if(config['ccweb.limitTime.begin'] && config['ccweb.limitTime.end']) {
                this.form.accessTime = config['ccweb.limitTime.begin'] + "-" + config['ccweb.limitTime.end'];
            }

            this.form.uploadPath = config['ccweb.upload.basePath'];
            this.form.mimeTypes = config['ccweb.upload.mimeTypes'];
            this.form.multiple = config['ccweb.upload.multiple'];
            this.form.maxSize = config['ccweb.upload.maxSize'];
            this.form.scalRatio = config['ccweb.download.thumb.scalRatio'];
            this.form.fixedWidth = config['ccweb.download.thumb.fixedWidth'];
            this.form.watermark = config['ccweb.download.thumb.watermark'];
        }
    }
</script>

<template lang="pug" src="@/views/settings/serviceSetting.pug" />
<style scoped>
    .settings .cctabpage {
        overflow: auto;
        height: calc(100vh - 212px);
        padding: 10px;
    }
    .settings .ivu-transfer {
        margin-top: 10px;
        margin-left: 47px;
    }
    .settings .cctabpage .ivu-divider-inner-text {
        color: #515a6e !important;
    }
    .ivu-poptip {
        margin-left: 5px;
    }
    .table-action {
        font-size: 16px !important;
        margin: 1px;
    }
    .ivu-form-item-error-tip {
        position: absolute;
        top: auto !important;
        left: auto !important;
    }
</style>