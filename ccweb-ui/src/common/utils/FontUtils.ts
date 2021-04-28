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

class FontUtils {

    getSystemFontList() {
        return [{text: "宋体",  value: "SimSun"},
            {text: "黑体",  value: "SimHei"},
            {text: "微软雅黑",  value: "Microsoft YaHei"},
            {text: "微软正黑体",  value: "Microsoft JhengHei"},
            {text: "新宋体",  value: "NSimSun"},
            {text: "新细明体",  value: "PMingLiU"},
            {text: "细明体",  value: "MingLiU"},
            {text: "标楷体",  value: "DFKai-SB"},
            {text: "仿宋",  value: "FangSong"},
            {text: "楷体",  value: "KaiTi"},
            {text: "仿宋_GB2312",  value: "FangSong_GB2312"},
            {text: "楷体_GB2312",  value: "KaiTi_GB2312"},
            {text: "儷黑 Pro",  value: "LiHei Pro Medium"},
            {text: "儷宋 Pro",  value: "LiSong Pro Light"},
            {text: "標楷體",  value: "BiauKai"},
            {text: "蘋果儷中黑",  value: "Apple LiGothic Medium"},
            {text: "蘋果儷細宋",  value: "Apple LiSung Light"},
            {text: "隶书",  value: "LiSu"},
            {text: "幼圆",  value: "YouYuan"},
            {text: "华文细黑",  value: "STXihei"},
            {text: "华文楷体",  value: "STKaiti"},
            {text: "华文宋体",  value: "STSong"},
            {text: "华文中宋",  value: "STZhongsong"},
            {text: "华文仿宋",  value: "STFangsong"},
            {text: "方正舒体",  value: "FZShuTi"},
            {text: "方正姚体",  value: "FZYaoti"},
            {text: "华文彩云",  value: "STCaiyun"},
            {text: "华文琥珀",  value: "STHupo"},
            {text: "华文隶书",  value: "STLiti"},
            {text: "华文行楷",  value: "STXingkai"},
            {text: "华文新魏",  value: "STXinwei"}];
    }
}

export default new FontUtils()
