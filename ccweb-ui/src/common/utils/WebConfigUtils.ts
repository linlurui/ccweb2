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

class WebConfigUtils {
    private packageConf = require('@/../package.json');

    public getWebConfig(): any {
        return eval("require(__dirname + '/" + this.packageConf.webConfig + "')");
    }

    public getRoot(): string {
        return this.getWebConfig().root;
    }

    public getDefault(): string {
        return this.getWebConfig().default;
    }

    public getDb(): string {
        return this.getWebConfig().db;
    }

    public getCharset(): string {
        return this.getWebConfig().charset;
    }

    public getLocals(): any {
        return this.getWebConfig().locals;
    }

    public getScope(): any {
        return this.getWebConfig().scope;
    }

    public getSites(): any {
        return this.getWebConfig().sites;
    }

    public getZip(): string {
        return this.getWebConfig().zip;
    }

    public getServer(): any {
        return this.packageConf.server;
    }

    public getProduction(): string {
        return this.getWebConfig().production;
    }
}

export default new WebConfigUtils()
