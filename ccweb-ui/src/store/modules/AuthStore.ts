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

import { Module, VuexModule, Mutation, Action } from 'vuex-module-decorators';
import store from "../index";

@Module({
  namespaced: true,
  stateFactory: true,
  dynamic: true,
  name: "AuthStore",
  store,
})
export default class AuthStore extends VuexModule {
  public count = 12; //state

  get getCount() { //getter
    return this.count;
  }

  @Action({ commit: 'decrement' })
  public async decr() {
    return 3;
    console.log('auth testtt');
  }

  @Mutation
  private decrement(delta: number) {
    console.log('delta', delta);
    this.count -= delta;
  }
}
