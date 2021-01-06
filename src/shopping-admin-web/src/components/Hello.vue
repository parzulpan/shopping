<template>
  <div>
    <h1>hello, {{ name }}</h1>
    <el-radio v-model="radio" label="1">备选项</el-radio>
    <el-radio v-model="radio" label="2">备选项</el-radio>
  </div>
</template>

<script>
export default {
  name: 'Hello',
  data () {
    return {
      name: 'parzulpan',
      radio: '2'
    }
  }
}
</script>

<style scope>
</style>
