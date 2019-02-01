<template>
  <div>
    <input
      placeholder="/year/month/day"
      v-model="ymd"
      @keyup.enter="submit"
    />
  </div>
</template>

<script>
export default {
  name: "DateSelector",
  data() {
    return {
      ymd: ""
    };
  },
  methods: {
    submit: function() {
      this.$emit("dayChanged", this.ymd);
      this.ymd = "";
    }
  }
};
</script>