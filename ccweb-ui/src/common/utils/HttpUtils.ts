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


import axios, {AxiosRequestConfig} from 'axios'
class HttpUtils {

    async get(url: string, config?: AxiosRequestConfig) {
        let data = null;
        await axios.get(url, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async post(url: string, params?: any, config?: AxiosRequestConfig) {
        let data:any = null;
        await axios.post(url, params, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async put(url: string, params?: any, config?: AxiosRequestConfig) {
        let data = null;
        await axios.put(url, params, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async delete(url: string, config?: AxiosRequestConfig) {
        let data = null;
        await axios.delete(url, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async upload(url: string, file: File, config?: AxiosRequestConfig) {
        let forms = new FormData()
        if(!config) {
            config = {};
        }

        if(!config.headers) {
            config.headers = {};
        }

        config.headers['Content-Type'] = 'application/x-www-form-urlencoded';

        forms.append('file',file);

        let data = null;
        await axios.post(url, forms, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async options(url: string, config?: AxiosRequestConfig) {
        let data = null;
        await axios.options(url, config).then(result => {
            if (!result) {
                return;
            }
            data = result;
        });

        return data;
    }

    async patch(url: string, data: any, config?: AxiosRequestConfig) {
        let result = null;
        await axios.patch(url, data, config).then(response => {
            if (!response) {
                return;
            }
            result = response;
        });

        return result;
    }

    async request(config: AxiosRequestConfig) {
        let result = null;
        await axios.request(config).then(response => {
            if (!response) {
                return;
            }
            result = response;
        });

        return result;
    }
}

export default new HttpUtils()
