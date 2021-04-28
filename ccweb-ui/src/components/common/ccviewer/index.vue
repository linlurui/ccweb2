<script lang="ts">
    import {Component, Prop, Vue, Watch} from 'vue-property-decorator'
    import 'viewerjs/dist/viewer.css'
    import Viewer from 'v-viewer'
    import pdf from 'vue-pdf'
    import HttpUtils from "@/common/utils/HttpUtils"
    import mammoth from 'mammoth'
    const docx = mammoth;
    import XLSX, {WorkSheet} from 'xlsx'
    Vue.use(Viewer)

    @Component({components: {
        'pdf': pdf
    }})
    export default class CCViewer extends Vue {

        @Prop() public id: string | undefined;
        @Prop() public srcList: string[] | undefined;
        @Prop() public type: string | undefined;
        @Prop() public pageNum: number | undefined;
        @Prop() public pageCount: number | undefined;
        @Prop() public serverPageTurning: boolean | undefined;
        @Prop() public options: any | undefined;
        @Prop() public suffix: string | undefined;
        @Prop() public convertType: 'html' | 'pdf' | undefined;

        private touch_p: any = {
            c_x : 0,
            c_y : 0,
            hasbacked : false
        }

        constructor() {
            super()
        }

        loadPdfHandler (e: any) {
            this.pageNum = 1;
        }

        getSrc(src: string) {

            if(!src) {
                return src;
            }

            if(!this.pageNum) {
                this.pageNum = 1;
            }

            if(this.serverPageTurning) {
                return src + '/' + (this.pageNum > 0 ? this.pageNum : 1);
            }

            return src;
        }

        @Watch("pageNum")
        changePageNum() {
            if(this.pageNum && this.type == "DOCUMENT") {
                if(this.suffix == "XLS" || this.suffix == "XLSX") {
                    let viewer:any = this.$el.querySelector('#'+this.id);
                    let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
                    if(!canvas) {
                        return;
                    }
                    let xlsViewer:any = canvas.querySelector('.xls-viewer');
                    if(!xlsViewer) {
                        return;
                    }

                    let xlsViewerSheets:any = xlsViewer.querySelector('.xls-viewer-sheets');
                    if(!xlsViewerSheets) {
                        return;
                    }

                    let iframes: Array<any> = xlsViewerSheets.getElementsByTagName('iframe');
                    if(!iframes || !iframes.length) {
                        return;
                    }

                    for(let i=0; i<iframes.length; i++) {
                        if(i==(this.pageNum - 1)) {
                            iframes[i].style.display = 'block';
                        }
                        else {
                            iframes[i].style.display = 'none';
                        }
                    }
                }
            }
        }

        onView() {
            let viewer:any = this.$el.querySelector('#'+this.id);
            viewer.$viewer.options.toolbar = this.isShowToolbar();
            viewer.$viewer.show(); //先显示后面才能查找到翻页按钮
            let canvas:any = viewer.$viewer.viewer.getElementsByClassName('viewer-canvas');
            if(!canvas || !canvas.length || canvas.length<1) {
                return;
            }
            canvas = canvas[0];

            if(!this.srcList) {
                return;
            }

            if(this.type == "VIDEO") {
                this.serverPageTurning = false;
                let html = '<video autoplay="autoplay" controls="controls">';
                for(let i=0; i<this.srcList.length; i++) {
                    if(!this.srcList[i]) {
                        continue;
                    }
                    html += '<source src="' + this.getSrc(this.srcList[i]) + '"/>';
                }
                html += '</video>';
                canvas.innerHTML = html;
            }
            else if(this.type == "AUDIO") {
                viewer.$viewer.options.toolbar = false;
                let html = '<audio autoplay="autoplay" controls="controls">';
                for(let i=0; i<this.srcList.length; i++) {
                    if(!this.srcList[i]) {
                        continue;
                    }
                    html += '<source src="' + this.getSrc(this.srcList[i]) + '"/>';
                }
                html += '</audio>';
                canvas.innerHTML = html;
            }
            else if(this.suffix == "HTM" || this.suffix == "HTML") {
                this.showHtmlViewer();
            }
            else if(this.suffix == "TXT" || this.suffix == "LOG") {
                if(this.convertType == "html") {
                    this.showTxtViewer();
                }
                else if(this.convertType == "pdf") {
                    this.showPDFViewer();
                }
                else {
                    this.showDocViewer();
                }
            }
            else if(this.suffix == "DOC" || this.suffix == "DOCX") {
                if(this.convertType == "html") {
                    this.showHtmlViewer()
                }
                else if(this.convertType == "pdf") {
                    this.showPDFViewer();
                }
                else {
                    this.showDocViewer();
                }
            }
            else if(this.suffix == "XLS" || this.suffix == "XLSX") {
                if(this.convertType == "html") {
                    this.showHtmlViewer()
                }
                else if(this.convertType == "pdf") {
                    this.showPDFViewer();
                }
                else {
                    this.showXlsViewer();
                }
            }
            else if(this.suffix=='PDF') {
                this.showPDFViewer();
            }

            if(viewer.$viewer.toolbar.querySelector('.viewer-prev')) {
                viewer.$viewer.toolbar.querySelector('.viewer-prev').removeEventListener('click', this.prev);
                viewer.$viewer.toolbar.querySelector('.viewer-prev').addEventListener('click', this.prev);
            }

            if(viewer.$viewer.toolbar.querySelector('.viewer-next')) {
                viewer.$viewer.toolbar.querySelector('.viewer-next').removeEventListener('click', this.next);
                viewer.$viewer.toolbar.querySelector('.viewer-next').addEventListener('click', this.next);
            }
        }

        prev() {
            this.touch_p.hasbacked = true;
            if(!this.pageNum) {
                this.pageNum = 1;
            }

            if (this.pageNum > 1) {
                this.pageNum = this.pageNum - 1;
            } else {
                this.pageNum = 1;
            }
            this.touch_p.hasbacked = false;
        }

        next() {
            this.touch_p.hasbacked = true;
            if(!this.pageNum) {
                this.pageNum = 1;
            }

            if (this.pageNum < 1) {
                this.pageNum = 1;
            } else {
                if(!this.pageCount || this.pageNum<this.pageCount) {
                    this.pageNum = this.pageNum + 1;
                }
                else {
                    this.pageNum = this.pageCount;
                }
            }
            this.touch_p.hasbacked = false;
        }

        touches(ev: any){
            if(ev.touches.length==1){
                switch(ev.type){
                    case 'touchstart':
                        // if(console) {
                        //     console.log('Touch start('+ev.touches[0].clientX+', '+ev.touches[0].clientY+')');
                        // }
                        this.touch_p.c_x = ev.touches[0].clientX;
                        this.touch_p.c_y = ev.touches[0].clientY;
                        ev.preventDefault();
                        break;
                    case 'touchend': //未成功触发，未找到原因
                        // if(console) {
                        //     console.log('Touch end(' + ev.changedTouches[0].clientX + ', ' + ev.changedTouches[0].clientY + ')');
                        // }
                        break;
                    case 'touchmove':
                        let tempX = ev.changedTouches[0].clientX;
                        let tempY = ev.changedTouches[0].clientY;
                        let diff_x = tempX - this.touch_p.c_x;
                        let diff_y = Math.abs(tempY - this.touch_p.c_y);
                        //x轴方向移动超过150 纵轴方向移动小于30
                        if(!this.touch_p.hasbacked && diff_x > 150 && diff_y < 30){
                            this.prev();
                        }
                        else if(!this.touch_p.hasbacked && diff_x < 150 && diff_y < 30){
                            this.next();
                        }
                        break;

                }
            }
        }

        getIcon() {
            switch (this.type) {
                case 'IMAGE':
                    return "md-image";
                case 'VIDEO':
                    return "logo-youtube";
                case 'AUDIO':
                    return "md-volume-mute";
                default:
                    return "md-document";
            }
        }

        onError() {
            if(!this.pageNum) {
                this.pageNum = 1;
                return;
            }

            this.pageCount = this.pageNum;
            this.pageNum--;
            if(this.pageNum<1) {
                this.pageNum = 1;
            }
        }

        private isShowToolbar() {
            switch (this.type) {
                case 'VIDEO':
                case 'AUDIO':
                    return false;
                default:
                    if(this.suffix=='DOC' || this.suffix=='DOCX') {
                        return false;
                    }

                    return true;
            }
        }

        private showPDFViewer() {
            let viewer:any = this.$el.querySelector('#'+this.id);
            let ele: Element | null = this.$el.querySelector('#PDF_'+this.id);

            let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
            if(!canvas) {
                return;
            }

            if(ele && !canvas.innerHTML) {
                viewer.$viewer.canvas.appendChild(ele);
                viewer.$viewer.canvas.querySelector('#PDF_' + this.id).style['display']='block';

                ele.removeEventListener('touchstart', this.touches);
                ele.removeEventListener('touchend', this.touches);
                ele.removeEventListener('touchmove', this.touches);
                ele.addEventListener('touchstart', this.touches);
                ele.addEventListener('touchend', this.touches);
                ele.addEventListener('touchmove', this.touches);
            }

            let li = document.createElement("li");
            li.innerText = "共" + this.pageCount + "页，当前页：" + this.pageNum;
            viewer.$viewer.navbar.querySelector('ul').innerHTML = "";
            viewer.$viewer.navbar.querySelector('ul').appendChild(li);
            viewer.$viewer.toolbar.querySelector('.viewer-zoom-in').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-zoom-out').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-one-to-one').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-reset').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-play').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-rotate-left').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-rotate-right').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-flip-horizontal').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-flip-vertical').style.display = 'none';
        }

        private showHtmlViewer() {
            if(!this.srcList) {
                return;
            }

            let viewer:any = this.$el.querySelector('#'+this.id);
            let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
            if(!canvas) {
                return;
            }

            canvas.innerHTML = "<div class='ccviewer-loading'>加载中...</div>";
            viewer.$viewer.options.toolbar = false;
            viewer.$viewer.options.navbar = false;
            HttpUtils.get(this.srcList[0]).then((data: any)=>{
                let iframe: HTMLIFrameElement = document.createElement('iframe');
                iframe.className = 'html-viewer';
                canvas.innerHTML = "";
                canvas.appendChild(iframe);
                canvas.getElementsByTagName('iframe')[0].contentDocument.getElementsByTagName('body')[0].innerHTML = data;
            })
        }

        private showXlsViewer() {
            if(!this.srcList) {
                return;
            }

            let viewer:any = this.$el.querySelector('#'+this.id);
            let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
            if(!canvas) {
                return;
            }

            canvas.innerHTML = "<div class='ccviewer-loading'>加载中...</div>";
            HttpUtils.get(this.srcList[0], { responseType: 'arraybuffer' }).then((data: any)=>{
                let bytes = new Uint8Array(data);
                let workbook = XLSX.read(bytes, {type: 'array'});
                let sheetNames = workbook.SheetNames;
                let xlsViewer: HTMLElement = document.createElement('div');
                xlsViewer.className = 'xls-viewer';
                canvas.innerHTML = "";
                let xlsSheets: HTMLElement = document.createElement('div');
                xlsSheets.className = 'xls-viewer-sheets';
                xlsViewer.appendChild(xlsSheets);
                canvas.appendChild(xlsViewer);
                this.pageCount = sheetNames.length;
                for(let i=0; i<sheetNames.length; i++) {
                    let sheetName: string = sheetNames[i];
                    let worksheet: WorkSheet = workbook.Sheets[sheetName];
                    let sheet: HTMLIFrameElement = document.createElement('iframe');
                    sheet.setAttribute('id', 'sheet' + i);
                    sheet.className = 'xls-viewer-sheet';
                    sheet.style.display = 'none';
                    if(i==0) {
                        sheet.style.display = 'block';
                    }

                    let html: string = XLSX.utils.sheet_to_html(worksheet);
                    xlsSheets.appendChild(sheet);
                    let sheetTable: HTMLElement = document.createElement('div');
                    sheetTable.className = 'xls-viewer-sheet-table'
                    sheetTable.innerHTML = html;
                    // @ts-ignore
                    sheet.contentDocument.body.appendChild(sheetTable);
                    let sheetNamesDiv: HTMLElement = document.createElement('div');
                    sheetNamesDiv.className = 'xls-viewer-sheet-names';
                    let span: HTMLElement = document.createElement('span');
                    span.innerText = sheetName;
                    sheetNamesDiv.appendChild(span);
                    // @ts-ignore
                    sheet.contentDocument.body.appendChild(sheetNamesDiv)
                    sheet.setAttribute("title", sheetName);

                    let style: HTMLStyleElement = document.createElement('style');
                    style.setAttribute("type", "text/css");
                    style.innerHTML = "table {\n" +
                        "    width: 100%;\n" +
                        "    background: #ccc;\n" +
                        "    margin: 10px auto;\n" +
                        "    border-collapse: collapse;\n" +
                        "    font-size: 0.85rem;\n" +
                        "}\n" +
                        "th,td {\n" +
                        "    height: 25px;\n" +
                        "    line-height: 25px;\n" +
                        "    text-align: center;\n" +
                        "    border: 1px solid #ccc;\n" +
                        "}\n" +
                        "th {\n" +
                        "    background: #eee;\n" +
                        "    font-weight: normal;\n" +
                        "}\n" +
                        "tr {\n" +
                        "    background: #fff;\n" +
                        "}\n" +
                        "tr:hover {\n" +
                        "    background: #bfa;\n" +
                        "}\n" +
                        "[t=s] {\n" +
                        "    min-width: 150px;\n" +
                        "}\n" +
                        "[t=n] {\n" +
                        "    min-width: 70px;\n" +
                        "}\n" +
                        "[t=d] {\n" +
                        "    min-width: 125px;\n" +
                        "}" + "::-webkit-scrollbar-track-piece {\n" +
                        "    background-color:#d8d8d8;\n" +
                        "}\n" +
                        "::-webkit-scrollbar {\n" +
                        "    width:5px;\n" +
                        "    height:5px;\n" +
                        "}\n" +
                        "::-webkit-scrollbar-thumb {\n" +
                        "    background-color:#828ea9;\n" +
                        "    background-clip:padding-box;\n" +
                        "    min-height:28px;\n" +
                        "}\n" +
                        "::-webkit-scrollbar-thumb:hover {\n" +
                        "    background-color:#297ad0;\n" +
                        "}\n" + "body {\n" +
                        "    overflow: hidden;\n" +
                        "}\n" + ".xls-viewer-sheet-table {\n" +
                        "    overflow: auto;\n" +
                        "    width: 100%;\n" +
                        "    height: calc(100vh - 45px);\n" +
                        "}\n" +
                        ".xls-viewer-sheet-names span {\n" +
                        "    display: inline-block;\n" +
                        "    border: green 2px solid;\n" +
                        "    width: auto;\n" +
                        "    padding: 2px;\n" +
                        "    margin-top: 2px;\n" +
                        "    font-size: 12px;\n" +
                        "    font-weight: bold;\n" +
                        "}";

                    // @ts-ignore
                    sheet.contentDocument.head.appendChild(style);
                }
            })

            let li = document.createElement("li");
            li.innerText = "共" + this.pageCount + "页，当前页：" + this.pageNum;
            viewer.$viewer.navbar.querySelector('ul').innerHTML = "";
            viewer.$viewer.navbar.querySelector('ul').appendChild(li);
            viewer.$viewer.toolbar.querySelector('.viewer-zoom-in').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-zoom-out').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-one-to-one').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-reset').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-play').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-rotate-left').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-rotate-right').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-flip-horizontal').style.display = 'none';
            viewer.$viewer.toolbar.querySelector('.viewer-flip-vertical').style.display = 'none';
        }

        private showDocViewer() {
            if(!this.srcList) {
                return;
            }

            let viewer:any = this.$el.querySelector('#'+this.id);
            let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
            if(!canvas) {
                return;
            }

            canvas.innerHTML = "<div class='ccviewer-loading'>加载中...</div>";
            HttpUtils.get(this.srcList[0], { responseType: 'arraybuffer' }).then((data: any)=>{
                let bytes = new Uint8Array(data);
                docx.convertToHtml({ arrayBuffer: bytes })
                    .then((resultObject: any)=> {
                        let iframe: HTMLIFrameElement = document.createElement('iframe');
                        iframe.className = 'doc-viewer';
                        canvas.innerHTML = "";
                        canvas.appendChild(iframe);
                        canvas.getElementsByTagName('iframe')[0].contentDocument.getElementsByTagName('body')[0].innerHTML = resultObject.value;
                    })
            })
        }

        private showTxtViewer() {
            if(!this.srcList) {
                return;
            }

            let viewer:any = this.$el.querySelector('#'+this.id);
            let canvas:any = viewer.$viewer.viewer.querySelector('.viewer-canvas');
            if(!canvas) {
                return;
            }

            canvas.innerHTML = "<div class='ccviewer-loading'>加载中...</div>";
            viewer.$viewer.options.toolbar = false;
            viewer.$viewer.options.navbar = false;
            HttpUtils.get(this.srcList[0]).then((data: any)=>{
                let iframe: HTMLIFrameElement = document.createElement('iframe');
                iframe.className = 'html-viewer';
                canvas.innerHTML = "";
                canvas.appendChild(iframe);
                if(data) {
                    data = data.replace(/(\r\n|\r|\n)/gmi, "<br/>");
                }
                canvas.getElementsByTagName('iframe')[0].contentDocument.getElementsByTagName('body')[0].innerHTML = data;
            })
        }
    }
</script>


<style src="./styles/ccviewer.css" />
<template lang="pug" src="./views/ccviewer.pug" />