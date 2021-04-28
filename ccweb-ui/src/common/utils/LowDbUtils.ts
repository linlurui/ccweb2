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


import WebConfigUtils from "@/common/utils/WebConfigUtils";
import {Message} from 'iview'
import FileUtils from "@/common/utils/FileUtils";
let database:any = null;


class LowDbUtils {

    use(dbname: string, sourcePath?: string) : LowDbUtils{
        if(!dbname){
            dbname = 'test';
        }

        let lowdb = require('lowdb');
        let FileSync = eval("require('lowdb/adapters/FileSync')");
        let path = sourcePath ? sourcePath : WebConfigUtils.getRoot() + "/" + WebConfigUtils.getDb();
        const adapter = new FileSync(path + dbname + '.json');
        FileUtils.ensureDir(path);
        if(!FileUtils.exists(adapter.source)) {
            FileUtils.saveTextFile(adapter.source, "{}");
        }
        database = lowdb(adapter);

        return this;
    }

    getData(key: string | undefined, whereSet?:any){
        try{
            if(!key) {
                return;
            }

            let db = database.get(key);
            if(!db) {
                return;
            }

            if(whereSet) {
                return db.find(whereSet).value();
            }

            return db.value();
        }
        catch(err){
            (<any>Message).error(err);
        }
    }

    setData(key: string | undefined, valueSet:any){
        try{
            if(!key) {
                return;
            }

            let db = database.get(key);
            if(!db || !db.value() || !Array.isArray(db.value())) {
                database.set(key, valueSet).write();
                return;
            }

            for(let i=0; i<valueSet.length; i++) {
                db.push(valueSet[i]).write();
            }
        }
        catch(err){
            (<any>Message).error(err);
        }
    }

    unset(key: string | undefined) {
        if(!key) {
            return;
        }

        database.unset(key).write();
    }

    update(key: string | undefined, whereSet:any, valueSet:any) {
        if(!key) {
            return;
        }

        database.get(key)
            .find(whereSet)
            .assign(valueSet)
            .write();
    }

    remove(key: string | undefined, whereSet:any) {
        if(!key) {
            return;
        }

        database.get(key)
            .remove(whereSet)
            .write();
    }

    add(key: string | undefined, valueSet:any) {
        if(!key) {
            return;
        }

        let db = database.get(key);
        if(!db || !db.value() || !Array.isArray(db.value())) {
            database.set(key, [ valueSet ]).write();
            return;
        }

        db.push(valueSet).write();
    }
}

export default new LowDbUtils()
