<template>
<div>
  <div>
    <button v-for="year in years" v-on:click="yearSelected(year.year)">{{year.year}} ({{year.count}})</button>
  </div>
  <div>
    <button v-for="month in months" v-on:click="monthSelected(month.yearMonth)">{{month.yearMonth.slice(-2)}} ({{month.count}})</button>
  </div>
  <div>
    <button v-for="day in days" v-on:click="daySelected(day.date)">{{day.date.slice(-2)}} ({{day.count}})</button>
  </div>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "DateSelector",
  data() {
    return {
      selectedYear: null,
      selectedMonth: null,
      selectedDate: null,
      years: [],
      months: [],
      days: []
    };
  },
  mounted() {
      this.updateYears();
  },
  methods: {
    updateYears: function() {
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" }).then(result => {
            this.years = result.data;
        }, error => {
            this.years = [];
        });
    },
    updateMonths: function() {                                    
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" + this.selectedYear + "/" }).then(result => {
            this.months = result.data;
        }, error => {
            this.months = [];
        });
    },
    updateDays: function() {                                    
        axios({ method: "GET", "url": "http://localhost:8080/rest/mediafiles/" + this.selectedYear + "/" + this.selectedMonth + "/" }).then(result => {
            this.days = result.data;
        }, error => {
            this.days = [];
        });
    },
    yearSelected: function(newYear) {
      this.selectedYear = newYear;
      this.updateMonths();
      this.days = [];
      this.$emit("dayChanged", null);
    },
    monthSelected: function(newYearMonth) {
      this.selectedMonth = newYearMonth.slice(-2);
      this.updateDays();
      this.$emit("dayChanged", null);
    },
    daySelected: function(newDate) {
      this.selectedDate = newDate;
      this.$emit("dayChanged", this.selectedDate);
    },
  }
};
</script>