import {Store} from "vuex";
import {getModule, VuexModule} from "vuex-module-decorators/dist/types/vuexmodule";
declare type ConstructorOf<C> = {
    new (...args: any[]): C;
};
import { Vue } from "vue-property-decorator";

function getStore<M extends VuexModule>(vue: Vue, moduleClass: ConstructorOf<any>, store?: Store<any>): M {
    const myStore = getModule(moduleClass);
    myStore.$vue = vue;
    myStore.set = (key: string, value: any)=> {
        myStore.$vue.$set(myStore.$vue, key, value);
    }

    myStore.delete = (Vue: any, vue: any)=> {
        myStore.$vue.$delete(Vue, vue);
    }

    return myStore;
}

export default getStore;
