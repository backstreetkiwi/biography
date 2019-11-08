<template>
  <div class="gallery-toggle-albums">
    <a href="#" v-if="!albumsOn" v-on:click="albumsSwitchedOn()">ALBUMS OFF</a>
    <a href="#" v-if="albumsOn" v-on:click="albumsSwitchedOff()">ALBUMS ON</a>
  </div>
</template>

<script>
export default {
  name: "GalleryToggleAlbums",
  props: {
    albumsOn: {
      type: Boolean,
      default: () => false
    }
  },
  methods: {
    albumsSwitchedOn: function() {
      this.$emit("toggled", true);
    },
    albumsSwitchedOff: function() {
      this.$emit("toggled", false);
    },
  }
};
</script>

<style scoped>
div.gallery-toggle-albums {
    float: left;
    width: 200px;
}
</style>