<template lang="pug">
#app
    router-view
    Spin(fix, v-show="loading")
        #cclogo
            #translation
                #logoImg
        #waiting 正在启动服务，请稍候...
</template>
<script lang="ts">
    import {Vue, Component, Watch} from 'vue-property-decorator'
    import axios, {AxiosResponse} from 'axios'
    import FileUtils from "./common/utils/FileUtils"
    import DatasourceStore from "./components/common/cctable/store/DatasourceStore"
    import CCTestStore from "@/components/common/cctest/store/CCTestStore"
    import {getModule} from "vuex-module-decorators"
    import WebConfigUtils from "@/common/utils/WebConfigUtils";
    const datasourceStore = getModule(DatasourceStore)
    const cctestStore = getModule(CCTestStore)

    @Component({
        components: {
        }
    })

    export default class App extends Vue {

        private loading:boolean;
        private isMounted:boolean = false;
        constructor() {
            super();
            this.loading = true;
            this.onReady();
        }

        mounted(): void {
            this.isMounted = true;
        }

        onReady(){
            let logPath = 'service/ccweb.log';
            if (process.platform != 'win32' && !WebConfigUtils.getProduction()) {
                logPath = 'dist/' + logPath;
            }
            logPath = './' + logPath;

            if(FileUtils.exists(logPath)) {
                let text:string = FileUtils.readFile(logPath);
                if(text && text.indexOf("Tomcat started on port(s)")>0) {
                    this.init();
                    if(this.isMounted) {
                        this.loading = false;
                    }
                    else {
                        let _self = this;
                        let late = ()=>{
                            if(_self.isMounted) {
                                _self.loading = false;
                            }
                            else {
                                setTimeout(late, 2000);
                            }
                        }
                        late();
                    }
                    return;
                }
            }

            setTimeout(this.onReady, 5000);
        }

        async init() {
            let matchHttpPort = document.URL.match(/httpPort=(\d+)/);
            let httpPort = matchHttpPort && matchHttpPort[1] ? matchHttpPort[1] : '80';

            let matchHttpsPort = document.URL.match(/httpsPort=(\d+)/);
            let httpsPort = matchHttpsPort && matchHttpsPort[1] ? matchHttpsPort[1] : '443';

            let port = (WebConfigUtils.getServer().ssl ? httpsPort : httpPort);
            let protocol = (WebConfigUtils.getServer().ssl ? 'https' : 'http');

            axios.defaults.baseURL = protocol + '://' + WebConfigUtils.getServer().domain + ':' + port + '/api/' + WebConfigUtils.getServer().datasource; // 配置axios请求的地址
            axios.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
            axios.defaults.withCredentials = true;  //设置cross跨域 并设置访问权限 允许跨域携带cookie信息

            let result:any = await this.login();
            axios.defaults.headers['userkey'] = result.key;
            axios.defaults.headers['Authorization'] = result.token; // 设置请求头为 Authorization
            // 请求拦截器
            axios.interceptors.request.use(
                async (config) => {
                    // 每次发送请求之前判断vuex中是否存在token
                    // 如果不存在，则统一在http请求的header都加上token，这样后台根据token判断你的登录情况
                    // 即使本地存在token，也有可能token是过期的，所以在响应拦截器中要对返回状态进行判断
                    let result:any = window.localStorage.getItem("token");
                    if(!result) {
                        result = await this.login();
                    }
                    result = JSON.parse(result);
                    config.headers.Authorization = result.token;
                    config.headers.userkey = result.key;

                    return config;
                },
                error => {
                    return Promise.reject(error);
                }
            )

            axios.interceptors.response.use((response: AxiosResponse) => {
                    if (!this.checkResponse(response)) {
                        return Promise.reject(response);
                    }

                    if(response.headers["content-type"].indexOf("application/json")==0) {
                        if(response.data.data == undefined) {
                            return Promise.resolve(response.data);
                        }
                        return Promise.resolve(response.data.data);
                    }
                    else {
                        return Promise.resolve(response.data);
                    }
            },
            // 服务器状态码不是2开头的的情况
            // 这里可以跟你们的后台开发人员协商好统一的错误状态码
            // 然后根据返回的状态码进行一些操作，例如登录过期提示，错误提示等等
            // 下面列举几个常见的操作，其他需求可自行扩展
            error => {
                if(!error.response) {
                    if(error.message) {
                        console.log(error.message);
                        this.$Message.error(error.message);
                    }
                    return Promise.reject(error);
                }
                if (error.response.status) {
                    switch (error.response.status) {
                        // 401: 未登录
                        // 未登录则跳转登录页面，并携带当前页面的路径
                        // 在登录成功后返回当前页面，这一步需要在登录页操作。
                        case 401:
                            window.localStorage.setItem("token", "");
                            this.$Message.error("身份验证失败，请关闭重新进入")
                            break;

                        // 403 token过期
                        // 登录过期对用户进行提示
                        // 清除本地token和清空vuex中token对象
                        // 跳转登录页面
                        case 403:
                            window.localStorage.setItem("token", "");
                            this.$Message.error("登录过期，请关闭重新进入");
                            // 清除token
                            break;

                        // 404请求不存在
                        case 404:
                            this.$Message.error("您访问的链接不存在。");
                            break;
                        // 其他错误，直接抛出错误提示
                        default:
                            this.$Message.error(error.response.data.message);
                    }
                    return Promise.reject(error.response);
                }
            })

            Vue.prototype.$axios = axios;

            datasourceStore.load();
        }

        private checkResponse(response: AxiosResponse) {
            if(response.status == 200) {
                return true;
            }

            if(response.data && response.data.data && response.data.data.status!=0) {
                this.$Message.error(response.data.data.message);
                return false;
            }

            this.$Message.error('登录失败');
            return false;
        }

        private async login() {

            let yaml = datasourceStore.apiConfig;
            if (!yaml) {
                this.$Message.error('无法登录到后端');
            }

            let username = yaml.ccweb.security.admin.username;
            let password = yaml.ccweb.security.admin.password;

            let result = null;
            await axios.post('login', {"username": username, "password": password}).then(response => {
                if (!this.checkResponse(response)) {
                    return;
                }

                result = {token: response.data.data.aesToken, key: response.data.data.key};
                window.localStorage.setItem("token", JSON.stringify(result));
                console.log("admin login!!!")
            });

            return result;
        }
    }
</script>
<style>
    body {
        -webkit-user-select: none;
        user-select: none;
    }

    #waiting {
        color: #902290;
    }

    /* 特效 */
    :root{--animation-duration:1s;}
    #cclogo{
        position: absolute;
        bottom: 0;
        width: 100%;
    }
    #translation,#logoImg{
        border-radius: 50%;
        display:inline-block;
        width: 32px;
        height: 32px;
    }
    #translation{
        animation-name: translation;
        animation-duration: var(--animation-duration);
        animation-direction: alternate;
        animation-iteration-count: infinite;
        animation-timing-function: ease-out;
    }
    #logoImg{
        background-image:url(../public/logo3-32.png);
        background-size: 100%;animation-name: logoImg;
        animation-duration: calc(var(--animation-duration) * 5.3);
        margin-bottom: 12pt;
     }

    @keyframes translation {
        100% {transform: translateY(-55px);box-shadow:20px 350px  250px #444466;}
    }
    @keyframes logoImg {
        100% {
            transform:rotate(360deg)
        }
    }

    /* 遮罩 */
    .ivu-spin-fix {
        position: absolute;
        top:0;
        left: 0;
        z-index: 666;
        width: 100%;
        height:100%;
        background-color: hsla(0,0%,100%,0.2);
    }
</style>