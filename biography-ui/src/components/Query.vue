<template>
<div>
  <div>
    <input type="radio" id="queryAll" value="ALL" v-model="queryModifier" />
    <label for="queryAll">ALL</label>
    <input type="radio" id="queryAny" value="ANY" v-model="queryModifier" />
    <label for="queryAny">ANY</label>
    <input v-model="queryString"/>
    <button v-on:click="fireQuery()">Find!</button>
  </div>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "Query",
  data() {
    return {
      queryString: '',
      queryModifier: 'ALL'
    };
  },
  methods: {
    fireQuery: function() {
      this.$emit("queryFired", this.queryString, this.queryModifier);
    }
  }
};
</script>

<style scoped>
</style>