<template>
  <div class="gallery-toggle-description">
    <a href="#" v-if="!descriptionOn" v-on:click="descriptionSwitchedOn()">DESCRIPTION OFF</a>
    <a href="#" v-if="descriptionOn" v-on:click="descriptionSwitchedOff()">DESCRIPTION ON</a>
  </div>
</template>

<script>
export default {
  name: "GalleryToggleDescription",
  props: {
    descriptionOn: {
      type: Boolean,
      default: () => false
    }
  },
  methods: {
    descriptionSwitchedOn: function() {
      this.$emit("toggled", true);
    },
    descriptionSwitchedOff: function() {
      this.$emit("toggled", false);
    },
  }
};
</script>

<style scoped>
div.gallery-toggle-description {
    float: left;
    width: 200px;
}
</style>