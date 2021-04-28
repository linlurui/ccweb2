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

class ImageUtils {

    public convertBase64ToBlob (urlData: string): Blob {

        let type = urlData.replace(/^data:(image\/[\w\d]+);base64,([\w\d+/=]+)/gmi, "$1");
        let bytes = atob(urlData.split(',')[1]) // 去掉url的头，并转换为byte
        // 处理异常,将ascii码小于0的转换为大于0
        let ab = new ArrayBuffer(bytes.length)
        let ia = new Uint8Array(ab)
        for (let i = 0; i < bytes.length; i++) {
            ia[i] = bytes.charCodeAt(i)
        }
        return new Blob([ab], { type: type })
    }

    public async convertUrlToBase64Async(url: string) {
        return new Promise<any> ((resolve,reject) => {
            let image = new Image();
            image.onload = async ()=> {
                try {
                    let canvas = document.createElement('canvas');
                    canvas.width = image.naturalWidth;
                    canvas.height = image.naturalHeight;
                    // 将图片插入画布并开始绘制
                    let content = canvas.getContext('2d');
                    if(!content) {
                        throw Error('convertUrlToBase64 error: fail to create canvas!')
                    }
                    content.drawImage(image, 0, 0);
                    // result
                    let result = canvas.toDataURL('image/png');
                    resolve({url: url, base64: result});
                }
                catch (e) {
                    reject(e);
                }
            };
            // CORS 策略，会存在跨域问题
            image.setAttribute("crossOrigin",'Anonymous');
            image.src = url;
            // 图片加载失败的错误处理
            image.onerror = () => {
                reject(new Error('convertUrlToBase64 error'));
            };
        });
    }

    public async replaceBlogUrlToBase64(text: string) {
        if (text && text.trim()) {
            let allPromise = new Array();
            let regex = /blob:file:\/\/\/?[\w\d\-]+/gmi;
            let matches = text.match(regex);
            if (matches && matches.length > 0) {
                for (let i = 0; i < matches.length; i++) {
                    allPromise[i] = this.convertUrlToBase64Async(matches[i]);
                }
            }

            await Promise.all(allPromise).then((result: any[]) => {
                for (let i = 0; i < result.length; i++) {
                    text = text.replace(result[i].url, result[i].base64);
                }
            })

            let pattern = /&quot;(data:\w+\/[\w\d]+;base64,[\w\d+/=]+)&quot;/gmi;
            text = text.replace(pattern, "'$1'");

            return Promise.resolve(text);
        }

        return Promise.resolve("");
    }

    public replaceBase64ToBlogUrl(text: string | undefined): string | undefined {
        if(text) {
            let matches = text.match(/data:(image\/[\w\d]+);base64,([\w\d+/=]+)/gmi);
            if(matches && matches.length>0) {
                for(let i=0; i<matches.length; i++) {
                    let item: string = matches[i];
                    let blob = this.convertBase64ToBlob(item);
                    let url = URL.createObjectURL(blob);
                    text = text.replace(item, url);
                }
            }
        }

        return text;
    }
}

export default new ImageUtils()
