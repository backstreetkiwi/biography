<template>
<div class="batchConsole">{{content}}</div>
</template>

<script>
import axios from "axios";

export default {
  name: "BatchConsole",
  props: {
    content: null,
  },
  mounted() {
  },
  methods: {
  }
};
</script>

<style scoped>
div.batchConsole {
  width: 100%;
  height: 400px;
  color: white;
  background-color: black;
  white-space: pre;
  overflow: scroll;
  text-align: left;
  padding: 5px; 
  font-family: 'Courier New', Courier, monospace;
  font-size: 8pt;
}
</style>