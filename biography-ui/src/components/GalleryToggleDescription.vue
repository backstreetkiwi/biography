<template>
  <div>
    <a href="#" v-if="!descriptionOn" v-on:click="descriptionSwitchedOn()">D ON</a>
    <a href="#" v-if="descriptionOn" v-on:click="descriptionSwitchedOff()">D OFF</a>
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
</style>