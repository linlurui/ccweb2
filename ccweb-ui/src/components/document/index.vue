<script lang="ts">
    import {Vue, Component, Watch} from 'vue-property-decorator';

    @Component
    export default class Document extends Vue {

    }
</script>

<template>
    <webview style="width: 100vw; height: 100vh;" class="webview-body" src="https://github.com/linlurui/ccweb2/blob/main/README.md" allowpopups></webview>
</template>