<template>
<div>
  <div>
    <ul>
      <li v-on:click="startRebuildSolrIndex()">
        <a class="link" href="#">Start Rebuild Solr Index</a>
      </li>
      <li v-on:click="generateMissingThumbnails()">
        <a class="link" href="#">Generate missing Thumbnails</a>
      </li>
      <li v-for="(batch,index) in batches" v-on:click="batchSelected(index)">
        <a class="link" href="#">{{batch.title}} (started {{batch.startTime}}) {{batch.closed}}</a>
      </li>
    </ul>
  </div>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "BatchSelector",
  data() {
    return {
      selectedBatch: null,
      batches: [] 
    };
  },
  mounted() {
      this.updateBatches();
  },
  methods: {
    updateBatches: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/batch/" }).then(result => {
            this.batches = result.data;
        }, error => {
            this.batches = [];
        });
    },
    startRebuildSolrIndex: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/batch/start/rebuild-index/" }).then(result => {
          updateBatches();
        }, error => {
            alert(error.message);
        });
    },
    generateMissingThumbnails: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/batch/start/generate-missing-thumbnails/" }).then(result => {
          updateBatches();
        }, error => {
            alert(error.message);
        });
    },
    batchSelected: function(newBatch) {
      this.selectedBatch = newBatch;
      this.$emit("batchChanged", this.selectedBatch);
    }
  }
};
</script>

<style scoped>
</style>