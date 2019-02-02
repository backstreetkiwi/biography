<template>
  <div id="batches">
    <BatchSelector @batchChanged="setConsole"/>
    <BatchConsole :content="content"/>
  </div>
</template>

<script>
import axios from "axios";
import BatchSelector from './BatchSelector'
import BatchConsole from './BatchConsole'

export default {
  name: 'Batches',
  data() {
    return {
      content: null,
    };
  },
  components: {
    BatchSelector,
    BatchConsole,
  },
  methods: {
    setConsole: function(batch) {
      axios({ method: "GET", "url": "http://localhost:8080/rest/batch/" + batch + "/console" }).then(result => {
          this.content = result.data;
      }, error => {
          this.content = "";
      });
      this.content = batch + "asdfa";
    }
  }
}
</script>
<style>
</style>