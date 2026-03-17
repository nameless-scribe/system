<template>
  <div id="app">
    <template v-if="layout === 'admin'">
      <admin-layout>
        <router-view />
      </admin-layout>
    </template>
    <template v-else-if="layout === 'auth'">
      <router-view />
    </template>
    <template v-else>
      <default-layout>
        <router-view />
      </default-layout>
    </template>
  </div>
</template>

<script>
import DefaultLayout from './layouts/DefaultLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'

export default {
  name: 'App',
  components: {
    DefaultLayout,
    AdminLayout
  },
  computed: {
    layout () {
      if (this.$route.meta && this.$route.meta.layout) {
        return this.$route.meta.layout
      }
      return 'default'
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
