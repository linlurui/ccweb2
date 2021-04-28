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

class ViewportUtils {

    constructor() {
    }

    getViewportSize() {
        let viewportWidth: number | undefined;
        let viewportHeight: number | undefined;
        if (window.innerWidth) {
            viewportWidth = window.innerWidth;
            viewportHeight = window.innerHeight;
        } else if (document.documentElement && document.documentElement.clientWidth || document.body && document.body.clientWidth) {
            viewportWidth = document.documentElement && document.documentElement.clientWidth || document.body && document.body.clientWidth;
            viewportHeight = document.documentElement && document.documentElement.clientHeight  || document.body && document.body.clientHeight;
        }

        return {
            viewportWidth: viewportWidth ? viewportWidth : 0,
            viewportHeight: viewportHeight ? viewportHeight : 0
        }
    }

    cpx2px(cpx: number) {
        if (typeof cpx !== 'number') {
            console.error('Parameter must be a number');
            return;
        }
        const viewportWidth: number = this.getViewportSize().viewportWidth;
        const px = +(viewportWidth / 750 * cpx).toFixed(3);
        return px;
    }

    px2cpx(px: number) {

        if (typeof px !== 'number') {
            console.error('Parameter must be a number');
            return;
        }

        const viewportWidth = this.getViewportSize().viewportWidth;
        const cpx = +(750 / viewportWidth * px).toFixed(3);
        return cpx;
    }
}

export default new ViewportUtils()
